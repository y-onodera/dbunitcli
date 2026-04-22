#!/usr/bin/env bash

IS_WEB_PROXY=false
if echo "${JAVA_TOOL_OPTIONS:-}" | grep -q "jwt_"; then
  IS_WEB_PROXY=true
fi

CURL_PROXY_ARGS=""
if [ "$IS_WEB_PROXY" = true ]; then
  CURL_PROXY_ARGS="--proxy http://127.0.0.1:3128"
fi

if [ "$IS_WEB_PROXY" = true ]; then
  # ローカルプロキシを先に起動（JDK ダウンロードで使用するため）
  nohup python3 /home/user/dbunitcli/.claude/maven-proxy.py >> /tmp/maven-proxy.log 2>&1 &
  PROXY_PID=$!
  echo "Maven proxy setup complete (PID: $PROXY_PID)"

  # ポートが利用可能になるまで待機（最大5秒）
  for i in $(seq 1 10); do
    if nc -z 127.0.0.1 3128 2>/dev/null; then
      break
    fi
    sleep 0.5
  done
fi

# JDK 25 のインストール（現在のバージョンが 25 未満の場合）
REQUIRED_JAVA_VERSION=25
JDK25_DIR="$HOME/jdk-25"
JDK25_READY=false

CURRENT_JAVA_MAJOR=$(java -version 2>&1 | grep -oP '(?<=version ")[0-9]+' | head -1)
echo "Current Java major version: ${CURRENT_JAVA_MAJOR:-unknown}"

if [ -z "$CURRENT_JAVA_MAJOR" ] || [ "$CURRENT_JAVA_MAJOR" -lt "$REQUIRED_JAVA_VERSION" ]; then
  if [ -d "$JDK25_DIR/bin" ]; then
    echo "JDK 25 already installed at $JDK25_DIR"
    JDK25_READY=true
  else
    echo "Installing JDK 25..."
    ORACLE_URL="https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz"
    TMP_JDK=$(mktemp /tmp/jdk25-XXXXXX.tar.gz)
    rm -rf "$JDK25_DIR" && mkdir -p "$JDK25_DIR"
    # shellcheck disable=SC2086
    if curl -sL $CURL_PROXY_ARGS "$ORACLE_URL" -o "$TMP_JDK" && tar -xz -C "$JDK25_DIR" --strip-components=1 -f "$TMP_JDK"; then
      echo "JDK 25 installed at $JDK25_DIR"
      JDK25_READY=true
    else
      echo "Failed to install JDK 25, falling back to current JDK"
      rm -rf "$JDK25_DIR"
    fi
    rm -f "$TMP_JDK"
  fi

  if [ "$JDK25_READY" = true ]; then
    # 以降のシェルセッションでも有効になるよう .bashrc に追記
    if ! grep -q "JAVA_HOME.*jdk-25" "$HOME/.bashrc" 2>/dev/null; then
      cat >> "$HOME/.bashrc" << EOF

# JDK 25 (installed by Claude setup)
export JAVA_HOME="$JDK25_DIR"
export PATH="\$JAVA_HOME/bin:\$PATH"
EOF
    fi
    export JAVA_HOME="$JDK25_DIR"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "JAVA_HOME set to $JDK25_DIR"
  fi
else
  echo "Java $CURRENT_JAVA_MAJOR >= $REQUIRED_JAVA_VERSION, no installation needed"
fi

# Create ~/.mavenrc to set JAVA_HOME and optionally proxy settings
{
  [ "$JDK25_READY" = true ] && echo "JAVA_HOME=\"$JDK25_DIR\""
  if [ "$IS_WEB_PROXY" = true ]; then
    printf 'MAVEN_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=3128 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=3128 -Dhttp.nonProxyHosts= $MAVEN_OPTS"\n'
  fi
} > "$HOME/.mavenrc"
echo "~/.mavenrc created"

if [ "$IS_WEB_PROXY" = true ]; then
  mkdir -p "$HOME/.m2"
  cat > "$HOME/.m2/settings.xml" << 'EOF'
<settings>
 <proxies>
  <proxy>
   <id>local</id>
   <active>true</active>
   <protocol>https</protocol>
   <host>127.0.0.1</host>
   <port>3128</port>
  </proxy>
 </proxies>
</settings>
EOF
fi
