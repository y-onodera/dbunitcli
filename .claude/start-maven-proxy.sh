#!/usr/bin/env bash
# Claude on the web 環境のみ実行（JAVA_TOOL_OPTIONS に JWT プロキシが設定されている場合）
if ! echo "${JAVA_TOOL_OPTIONS:-}" | grep -q "jwt_"; then
  exit 0
fi

# JDK 25 のインストール（現在のバージョンが 25 未満の場合）
REQUIRED_JAVA_VERSION=25
JDK25_DIR="$HOME/jdk-25"
CURRENT_JAVA_MAJOR=$(java -version 2>&1 | grep -oP '(?<=version ")[0-9]+' | head -1)
echo "Current Java major version: ${CURRENT_JAVA_MAJOR:-unknown}"

if [ -z "$CURRENT_JAVA_MAJOR" ] || [ "$CURRENT_JAVA_MAJOR" -lt "$REQUIRED_JAVA_VERSION" ]; then
  if [ -d "$JDK25_DIR/bin" ]; then
    echo "JDK 25 already installed at $JDK25_DIR"
  else
    echo "Installing JDK 25..."
    mkdir -p "$JDK25_DIR"
    ADOPTIUM_URL="https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse"
    if curl -sL "$ADOPTIUM_URL" | tar -xz -C "$JDK25_DIR" --strip-components=1; then
      echo "JDK 25 installed at $JDK25_DIR"
    else
      echo "Failed to install JDK 25, falling back to current JDK"
      rm -rf "$JDK25_DIR"
    fi
  fi

  if [ -d "$JDK25_DIR/bin" ]; then
    # 以降のシェルセッションでも有効になるよう .bashrc に追記
    if ! grep -q "jdk-25" "$HOME/.bashrc" 2>/dev/null; then
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

nohup python3 /home/user/dbunitcli/.claude/maven-proxy.py >> /tmp/maven-proxy.log 2>&1 &
echo "Maven proxy setup complete (PID: $!)"

# Create ~/.mavenrc to override JAVA_TOOL_OPTIONS proxy settings
# MAVEN_OPTS flags are appended to JVM command line, overriding JAVA_TOOL_OPTIONS
cat > "$HOME/.mavenrc" << EOF
$([ -d "$JDK25_DIR/bin" ] && echo "JAVA_HOME=\"$JDK25_DIR\"")
MAVEN_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=3128 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=3128 -Dhttp.nonProxyHosts= \$MAVEN_OPTS"
EOF
echo "~/.mavenrc created"

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
