#!/usr/bin/env bash
# Claude on the web 環境のみ実行（JAVA_TOOL_OPTIONS に JWT プロキシが設定されている場合）
if ! echo "${JAVA_TOOL_OPTIONS:-}" | grep -q "jwt_"; then
  exit 0
fi

nohup python3 /home/user/dbunitcli/.claude/maven-proxy.py >> /tmp/maven-proxy.log 2>&1 &
echo "Maven proxy setup complete (PID: $!)"

# Create ~/.mavenrc to override JAVA_TOOL_OPTIONS proxy settings
# MAVEN_OPTS flags are appended to JVM command line, overriding JAVA_TOOL_OPTIONS
cat > "$HOME/.mavenrc" << 'EOF'
MAVEN_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=3128 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=3128 -Dhttp.nonProxyHosts= $MAVEN_OPTS"
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
