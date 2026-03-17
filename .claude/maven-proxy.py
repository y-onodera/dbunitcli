#!/usr/bin/env python3
"""Local proxy that adds auth when forwarding to upstream proxy."""
import socket, threading, os, base64, select, sys
from urllib.parse import urlparse

LOCAL_PORT = 3128
UPSTREAM = os.environ.get('https_proxy') or os.environ.get('HTTPS_PROXY')

if not UPSTREAM:
    sys.exit(0)

def get_upstream():
    p = urlparse(UPSTREAM)
    return p.hostname, p.port, p.username or '', p.password or ''

def handle(client):
    try:
        req = b''
        while b'\r\n\r\n' not in req:
            req += client.recv(4096)
        target = req.split(b'\r\n')[0].split()[1].decode()
        host, port = (target.split(':') + ['443'])[:2]
        proxy_host, proxy_port, user, pwd = get_upstream()
        auth = base64.b64encode(f"{user}:{pwd}".encode()).decode()
        upstream = socket.socket()
        upstream.connect((proxy_host, proxy_port))
        upstream.send(
            f"CONNECT {host}:{port} HTTP/1.1\r\n"
            f"Proxy-Authorization: Basic {auth}\r\n\r\n".encode()
        )
        resp = b''
        while b'\r\n\r\n' not in resp:
            resp += upstream.recv(4096)
        if b'200' in resp.split(b'\r\n')[0]:
            client.send(b'HTTP/1.1 200 Connection Established\r\n\r\n')
            peer = {client: upstream, upstream: client}
            # Keep sockets BLOCKING for sendall - non-blocking sendall loses data
            # Use select only to detect readability
            half_closed = set()
            while True:
                active = [s for s in [client, upstream] if s not in half_closed]
                if not active:
                    break
                r, _, _ = select.select(active, [], [], 60)
                if not r:
                    break
                for s in r:
                    try:
                        data = s.recv(65536)
                    except OSError:
                        half_closed.add(s)
                        half_closed.add(peer[s])
                        continue
                    if not data:
                        half_closed.add(s)
                        try:
                            peer[s].shutdown(socket.SHUT_WR)
                        except OSError:
                            half_closed.add(peer[s])
                    else:
                        try:
                            peer[s].sendall(data)
                        except OSError:
                            half_closed.add(s)
                            half_closed.add(peer[s])
    except:
        pass
    finally:
        for s in [client]:
            try:
                s.close()
            except OSError:
                pass

if __name__ == '__main__':
    srv = socket.socket()
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        srv.bind(('127.0.0.1', LOCAL_PORT))
    except OSError:
        sys.exit(0)  # Already running
    srv.listen(10)
    print(f"Local proxy on 127.0.0.1:{LOCAL_PORT}", flush=True)
    while True:
        c, _ = srv.accept()
        threading.Thread(target=handle, args=(c,), daemon=True).start()
