mod sidecar_log;

use sidecar_log::SidecarLog;
use std::{
    env, io,
    os::windows::{
        io::{AsRawHandle, FromRawHandle, OwnedHandle},
        process::CommandExt,
    },
    process::{Child, Command, Stdio},
    sync::{Arc, Mutex},
};
use tauri::{Manager, WindowEvent};
use tauri_plugin_cli::CliExt;
use windows_sys::Win32::System::{
    JobObjects::{
        AssignProcessToJobObject, CreateJobObjectW, JobObjectExtendedLimitInformation,
        SetInformationJobObject, JOBOBJECT_EXTENDED_LIMIT_INFORMATION,
        JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE,
    },
    Threading::CREATE_NO_WINDOW,
};
/// 起動したsidecar。ジョブのハンドルは本体の終了まで保持する（閉じるとOSがsidecarを終了させる）
struct Sidecar {
    child: Mutex<Child>,
    _job: Option<OwnedHandle>,
}
#[tauri::command]
fn open_directory(path: String) -> Result<(), String> {
    Command::new("explorer")
        .arg(path)
        .spawn()
        .map(|_| ())
        .map_err(|e| e.to_string())
}
/// sidecarの出力のうち、通し番号がsince以降の行と次に渡すsinceを返す（起動中の表示が差分を読む）
#[tauri::command]
fn sidecar_logs(since: usize, log: tauri::State<Arc<SidecarLog>>) -> (usize, Vec<String>) {
    log.since(since)
}
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_http::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_cli::init())
        .setup(|app: &mut tauri::App| {
            // 起動時のカレントディレクトリに依存しないよう、同梱リソース（exeと同じディレクトリ）から解決する。
            // sidecarのカレントディレクトリは変えない（workspace等の既定値"."の基準になっている）
            let backend = app.path().resource_dir()?.join("backend");
            let mut args = vec![format!("-Djava.home={}", backend.display())];

            match app.cli().matches() {
                Ok(matches) => {
                    add_arg(
                        &matches,
                        &mut args,
                        "port",
                        "-Dmicronaut.server.port=",
                        "8080",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "workspace",
                        "-Dyo.dbunit.cli.workspace=",
                        ".",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "dataset.base",
                        "-Dyo.dbunit.cli.dataset.base=",
                        ".",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "result.base",
                        "-Dyo.dbunit.cli.result.base=",
                        ".",
                    );
                }
                Err(e) => println!("{:?}", e),
            }

            args.extend(
                env::var("JAVA_TOOL_OPTIONS")
                    .map(|options| split_java_options(&options))
                    .unwrap_or_default(),
            );

            // 出力は画面へ出すために溜める。devはコンソールへもそのまま出す
            let mut child = Command::new(backend.join("dbunit-cli-sidecar.exe"))
                .creation_flags(CREATE_NO_WINDOW)
                .args(args)
                .stdout(Stdio::piped())
                .stderr(Stdio::piped())
                .spawn()
                .expect("Failed to spawn child process");
            let log = Arc::new(SidecarLog::default());
            if let Some(stdout) = child.stdout.take() {
                sidecar_log::forward(stdout, cfg!(debug_assertions).then(io::stdout), log.clone());
            }
            if let Some(stderr) = child.stderr.take() {
                sidecar_log::forward(stderr, cfg!(debug_assertions).then(io::stderr), log.clone());
            }
            app.manage(log);
            // 本体がクラッシュ・強制終了しても、ジョブのハンドルが閉じられるとOSがsidecarを終了させる
            let job = kill_on_close_job(&child)
                .inspect_err(|e| eprintln!("Failed to assign sidecar to job object: {e}"))
                .ok();
            app.manage(Sidecar {
                child: Mutex::new(child),
                _job: job,
            });
            Ok(())
        })
        .on_window_event(|window, event| {
            if matches!(event, WindowEvent::Destroyed) && window.label() == "main" {
                stop_sidecar(window.app_handle());
            }
        })
        .invoke_handler(tauri::generate_handler![open_directory, sidecar_logs])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
fn add_arg(
    matches: &tauri_plugin_cli::Matches,
    args: &mut Vec<String>,
    key: &str,
    arg: &str,
    default_value: &str,
) {
    if let Some(value) = matches.args.get(key) {
        let new_arg = match value.value.as_str() {
            Some(s) => format!("{}{}", arg, s),
            None => format!("{}{}", arg, default_value),
        };
        args.push(new_arg);
    }
}
/// 終了処理なので、既に終了していた等で止められなくても何もしない
fn stop_sidecar(app: &tauri::AppHandle) {
    if let Some(sidecar) = app.try_state::<Sidecar>() {
        if let Ok(mut child) = sidecar.child.lock() {
            let _ = child.kill();
        }
    }
}
/// JAVA_TOOL_OPTIONSをJVMと同じ規則で分割する。
/// 空白で区切り、'または"で囲んだ部分は空白を含めて1つの値にする（引用符は取り除く）
fn split_java_options(options: &str) -> Vec<String> {
    let mut result = Vec::new();
    let mut current = String::new();
    let mut in_token = false;
    let mut quote = None;
    for c in options.chars() {
        match quote {
            Some(q) if c == q => quote = None,
            Some(_) => current.push(c),
            None if c == '\'' || c == '"' => {
                quote = Some(c);
                in_token = true;
            }
            None if c.is_whitespace() => {
                if in_token {
                    result.push(std::mem::take(&mut current));
                    in_token = false;
                }
            }
            None => {
                current.push(c);
                in_token = true;
            }
        }
    }
    if in_token {
        result.push(current);
    }
    result
}
/// 閉じるとchildを終了させるジョブにchildを入れる。返したハンドルは本体の終了まで保持すること。
/// 本体のプロセスが終わればOSがハンドルを閉じるため、終了の仕方によらずsidecarが残らない
fn kill_on_close_job(child: &Child) -> io::Result<OwnedHandle> {
    // SAFETY: Win32 APIへ渡すポインタ・ハンドルは呼び出しの間だけ有効であればよく、
    // 作成したジョブのハンドルは直後にOwnedHandleへ移して所有権を一元化する
    unsafe {
        let raw = CreateJobObjectW(std::ptr::null(), std::ptr::null());
        if raw.is_null() {
            return Err(io::Error::last_os_error());
        }
        let job = OwnedHandle::from_raw_handle(raw);
        let mut info: JOBOBJECT_EXTENDED_LIMIT_INFORMATION = std::mem::zeroed();
        info.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
        if SetInformationJobObject(
            job.as_raw_handle(),
            JobObjectExtendedLimitInformation,
            &info as *const _ as *const _,
            std::mem::size_of::<JOBOBJECT_EXTENDED_LIMIT_INFORMATION>() as u32,
        ) == 0
        {
            return Err(io::Error::last_os_error());
        }
        if AssignProcessToJobObject(job.as_raw_handle(), child.as_raw_handle()) == 0 {
            return Err(io::Error::last_os_error());
        }
        Ok(job)
    }
}
#[cfg(test)]
mod tests {
    use super::split_java_options;

    #[test]
    fn split_java_options_splits_by_whitespace() {
        assert_eq!(
            split_java_options("  -Xmx1g \t-Da=b  "),
            vec!["-Xmx1g", "-Da=b"]
        );
    }

    #[test]
    fn split_java_options_keeps_quoted_whitespace() {
        assert_eq!(
            split_java_options(r#"-Dx="C:\Program Files\a" '-Dy=a b' -Dz=c"#),
            vec![r"-Dx=C:\Program Files\a", "-Dy=a b", "-Dz=c"]
        );
    }

    #[test]
    fn split_java_options_empty() {
        assert!(split_java_options("   ").is_empty());
    }
}
