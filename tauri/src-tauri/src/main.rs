// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::{process::{Command, Stdio}, sync::mpsc::sync_channel, thread};

use tauri::WindowEvent;

// Learn more about Tauri commands at https://tauri.app/v1/guides/features/command
#[tauri::command]
fn main() {
    let (tx,rx) = sync_channel::<i64>(1);
    let mut child = Command::new("backend/dbunit-cli-sidecar-x86_64-pc-windows-msvc.exe")
          .arg("-Djava.home=backend")
          .arg("-Dyo.dbunit.cli.sidecar.workspace=target")
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
    tauri::Builder::default()
        .on_window_event(move |event| match event.event() {
            WindowEvent::Destroyed => {
              tx.send(-1).expect("Failed to stop child process");
            }
            _ => {}
          })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
