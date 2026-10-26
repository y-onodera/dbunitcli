use std::{
    env, io,
    os::windows::{
        io::{AsRawHandle, FromRawHandle, OwnedHandle},
        process::CommandExt,
    },
    process::{Child, Command, Stdio},
    sync::mpsc::sync_channel,
    thread,
};
use tauri::{Manager, WindowEvent};
use tauri_plugin_cli::CliExt;
use windows_sys::Win32::System::JobObjects::{
    AssignProcessToJobObject, CreateJobObjectW, JobObjectExtendedLimitInformation,
    SetInformationJobObject, JOBOBJECT_EXTENDED_LIMIT_INFORMATION,
    JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE,
};
#[tauri::command]
fn open_directory(path: String) {
    Command::new("explorer").args([path]).spawn().unwrap();
}
pub fn run() {
    let (tx, rx) = sync_channel::<i64>(1);
    tauri::Builder::default()
        .plugin(tauri_plugin_http::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_cli::init())
        .plugin(tauri_plugin_shell::init())
        .setup(|app: &mut tauri::App| {
            // 起動時のカレントディレクトリに依存しないよう、同梱リソース（exeと同じディレクトリ）から解決する。
            // sidecarのカレントディレクトリは変えない（workspace等の既定値"."の基準になっている）
            let backend = app.path().resource_dir()?.join("backend");
            let mut args = vec![format!("-Djava.home={}", backend.display())];

            let mut java_tool_options_args = vec![];
            if let Ok(java_tool_options) = env::var("JAVA_TOOL_OPTIONS") {
                java_tool_options_args
                    .extend(java_tool_options.split_whitespace().map(String::from));
            }

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

            args.extend(java_tool_options_args);

            // 読まないパイプにするとバッファが埋まった時点でsidecarのログ出力がブロックし、処理全体が止まる。
            // devはコンソールへそのまま出し、配布版は捨てる（ログはsidecarのログファイルに残る）
            let output = || {
                if cfg!(debug_assertions) {
                    Stdio::inherit()
                } else {
                    Stdio::null()
                }
            };
            let mut child = Command::new(backend.join("dbunit-cli-sidecar.exe"))
                .creation_flags(0x08000000) // CREATE_NO_WINDOW
                .args(args)
                .stdout(output())
                .stderr(output())
                .spawn()
                .expect("Failed to spawn child process");
            // 本体がクラッシュ・強制終了しても、ジョブのハンドルが閉じられるとOSがsidecarを終了させる
            let job = match kill_on_close_job(&child) {
                Ok(job) => Some(job),
                Err(e) => {
                    eprintln!("Failed to assign sidecar to job object: {e}");
                    None
                }
            };
            thread::spawn(move || {
                // 本体の終了まで保持する（スレッドはウィンドウを閉じるまで終わらない）
                let _job = job;
                while let Ok(signal) = rx.recv() {
                    if signal == -1 {
                        child.kill().expect("Failed to stop child process");
                        break;
                    }
                }
            });
            Ok(())
        })
        .on_window_event(move |window, event| match event {
            WindowEvent::Destroyed => {
                if window.label() == "main" {
                    tx.send(-1).expect("Failed to stop child process");
                }
            }
            _ => {}
        })
        .invoke_handler(tauri::generate_handler![open_directory])
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
    if let Some(value) = matches.args.get(key).clone() {
        let new_arg = match value.value.as_str() {
            Some(s) => format!("{}{}", arg, s),
            None => format!("{}{}", arg, default_value),
        };
        args.push(new_arg);
    }
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
