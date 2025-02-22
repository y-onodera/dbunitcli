// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::{
    env,
    process::{Command, Stdio},
    sync::mpsc::sync_channel,
    thread,
};
use tauri::WindowEvent;
// Learn more about Tauri commands at https://tauri.app/v1/guides/features/command
#[tauri::command]
fn open_directory(path: String) {
    println!("{:?}", path);
    Command::new("explorer").args([path]).spawn().unwrap();
}
fn add_arg(
    matches: &tauri::api::cli::Matches,
    args: &mut Vec<String>,
    key: &str,
    arg: &mut String,
    default_value: &str,
) {
    if let Some(value) = matches.args.get(key).clone() {
        match value.value.as_str() {
            Some(s) => arg.push_str(s),
            None => arg.push_str(default_value),
        }
        args.push(arg.clone());
    }
}
fn main() {
    let (tx, rx) = sync_channel::<i64>(1);
    tauri::Builder::default()
        .setup(|app: &mut tauri::App| {
            let mut args = vec![String::from("-Djava.home=backend")];

            let mut java_tool_options_args = vec![];
            if let Ok(java_tool_options) = env::var("JAVA_TOOL_OPTIONS") {
                java_tool_options_args
                    .extend(java_tool_options.split_whitespace().map(String::from));
            }

            match app.get_cli_matches() {
                Ok(matches) => {
                    add_arg(
                        &matches,
                        &mut args,
                        "port",
                        &mut String::from("-Dmicronaut.server.port="),
                        "8080",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "workspace",
                        &mut String::from("-Dyo.dbunit.cli.workspace="),
                        ".",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "dataset.base",
                        &mut String::from("-Dyo.dbunit.cli.dataset.base="),
                        ".",
                    );
                    add_arg(
                        &matches,
                        &mut args,
                        "result.base",
                        &mut String::from("-Dyo.dbunit.cli.result.base="),
                        ".",
                    );
                }
                Err(e) => println!("{:?}", e),
            }

            args.extend(java_tool_options_args);

            let mut child = Command::new("backend/dbunit-cli-sidecar.exe")
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
        .on_window_event(move |event| match event.event() {
            WindowEvent::Destroyed => {
                tx.send(-1).expect("Failed to stop child process");
            }
            _ => {}
        })
        .invoke_handler(tauri::generate_handler![open_directory])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
