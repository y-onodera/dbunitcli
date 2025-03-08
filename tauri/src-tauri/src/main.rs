// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::{
    env,
    os::windows::process::CommandExt,
    process::{Command, Stdio},
    sync::mpsc::sync_channel,
    thread,
};
use tauri::WindowEvent;
use tauri_plugin_cli::CliExt;
#[tauri::command]
fn open_directory(path: String) {
    println!("{:?}", path);
    Command::new("explorer").args([path]).spawn().unwrap();
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
fn main() {
    let (tx, rx) = sync_channel::<i64>(1);
    tauri::Builder::default()
        .plugin(tauri_plugin_http::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_cli::init())
        .plugin(tauri_plugin_shell::init())
        .setup(|app: &mut tauri::App| {
            let mut args = vec![String::from("-Djava.home=backend")];

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

            let mut child = Command::new("backend/dbunit-cli-sidecar.exe")
                .creation_flags(0x08000000) // CREATE_NO_WINDOW
                .args(args)
                .stdout(Stdio::piped())
                .stderr(Stdio::piped())
                .spawn()
                .expect("Failed to spawn child process");
            thread::spawn(move || loop {
                let s = rx.recv();
                if s.unwrap() == -1 {
                    child.kill().expect("Failed to stop child process");
                }
            });
            Ok(())
        })
        .on_window_event(move |_, event| match event {
            WindowEvent::Destroyed => {
                tx.send(-1).expect("Failed to stop child process");
            }
            _ => {}
        })
        .invoke_handler(tauri::generate_handler![open_directory])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
