use std::{
    collections::VecDeque,
    io::{BufRead, BufReader, Read, Write},
    sync::{Arc, Mutex},
    thread,
};

/// 保持する行数。起動中の表示に使うだけなので、溢れた古い行は捨てる
const MAX_LINES: usize = 1000;

/// sidecarの標準出力・標準エラーの直近の行。行には通し番号を振り、差分だけ読めるようにする
#[derive(Default)]
pub struct SidecarLog {
    inner: Mutex<Lines>,
}

#[derive(Default)]
struct Lines {
    lines: VecDeque<String>,
    /// 次に追加する行の通し番号
    next: usize,
}

impl SidecarLog {
    fn push(&self, line: String) {
        if let Ok(mut log) = self.inner.lock() {
            if log.lines.len() == MAX_LINES {
                log.lines.pop_front();
            }
            log.lines.push_back(line);
            log.next += 1;
        }
    }

    /// 通し番号がsince以降の行と、次に渡すsinceを返す。既に捨てた行は飛ばす
    pub fn since(&self, since: usize) -> (usize, Vec<String>) {
        let Ok(log) = self.inner.lock() else {
            return (since, Vec::new());
        };
        let first = log.next - log.lines.len();
        let lines = log
            .lines
            .iter()
            .skip(since.saturating_sub(first))
            .cloned()
            .collect();
        (log.next, lines)
    }
}

/// 終わるまでoutputを読み、行をlogへ溜める（読まないパイプはバッファが埋まった時点でsidecarのログ出力をブロックする）。
/// consoleがあれば読んだ内容をそのまま書き出す。UTF-8でない出力も読み続けられるよう、不正なバイトは置き換える
pub fn forward(
    output: impl Read + Send + 'static,
    mut console: Option<impl Write + Send + 'static>,
    log: Arc<SidecarLog>,
) {
    thread::spawn(move || {
        let mut reader = BufReader::new(output);
        let mut buf = Vec::new();
        loop {
            buf.clear();
            match reader.read_until(b'\n', &mut buf) {
                Ok(0) | Err(_) => break,
                Ok(_) => {}
            }
            if let Some(console) = console.as_mut() {
                let _ = console.write_all(&buf);
            }
            log.push(String::from_utf8_lossy(&buf).trim_end().to_string());
        }
    });
}

#[cfg(test)]
mod tests {
    use super::{SidecarLog, MAX_LINES};

    fn log_of(count: usize) -> SidecarLog {
        let log = SidecarLog::default();
        for i in 0..count {
            log.push(i.to_string());
        }
        log
    }

    #[test]
    fn since_returns_lines_after_since() {
        let log = log_of(3);
        assert_eq!(log.since(0), (3, vec!["0".into(), "1".into(), "2".into()]));
        assert_eq!(log.since(2), (3, vec!["2".into()]));
        assert_eq!(log.since(3), (3, vec![]));
    }

    #[test]
    fn since_skips_dropped_lines() {
        let log = log_of(MAX_LINES + 2);
        let (next, lines) = log.since(0);
        assert_eq!(next, MAX_LINES + 2);
        assert_eq!(lines.len(), MAX_LINES);
        assert_eq!(lines[0], "2");
    }

    #[test]
    fn forward_reads_lines_lossily() {
        let log = std::sync::Arc::new(SidecarLog::default());
        super::forward(&b"a\r\nb\xff\nc"[..], None::<std::io::Sink>, log.clone());
        for _ in 0..100 {
            if log.since(0).0 == 3 {
                break;
            }
            std::thread::sleep(std::time::Duration::from_millis(10));
        }
        assert_eq!(
            log.since(0).1,
            vec!["a".to_string(), "b\u{fffd}".to_string(), "c".to_string()]
        );
    }
}
