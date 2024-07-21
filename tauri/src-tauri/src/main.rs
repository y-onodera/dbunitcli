// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::{process::{Command, Stdio}, sync::mpsc::sync_channel, thread};

use tauri::WindowEvent;

// Learn more about Tauri commands at https://tauri.app/v1/guides/features/command
#[tauri::command]
fn main() {
    let (tx,rx) = sync_channel::<i64>(1);
    tauri::Builder::default()
         .setup(|app: &mut tauri::App| {
            let mut args = vec!["-Djava.home=backend"];
            let mut arg1 = String::from("-Dmicronaut.server.port=");
            let mut arg2 = String::from("-Dyo.dbunit.cli.sidecar.workspace=");
            match app.get_cli_matches() {
              Ok(matches) => {
                if let Some(port)= matches.args.get("port").clone() {
                  match port.value.as_str() {
                    Some(s) => arg1.push_str(s),
                    None =>arg1.push_str("8080")
                  }                    
                  args.push(&arg1);
                }
                if let Some(workspace) = matches.args.get("workspace").clone() {
                  match workspace.value.as_str() {
                    Some(s) => arg2.push_str(s),
                    None =>arg2.push_str(".")
                  }                    
                  args.push(&arg2);
                }
              }
              Err(e) => println!("{:?}", e),
            }
            let mut child = Command::new("backend/dbunit-cli-sidecar.exe")
            .args(args)
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
            .spawn()
            .expect("Failed to spawn child process");
            thread::spawn(move || {
              loop{
                let s = rx.recv();
                if s.unwrap()==-1 {
                  child.kill().expect("Failed to stop child process");
                }
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
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
