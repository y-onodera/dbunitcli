#!/usr/bin/env bash
if [ -z "$https_proxy" ] && [ -z "$HTTPS_PROXY" ]; then
  exit 0
fi

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

nohup python3 /home/user/dbunitcli/.claude/maven-proxy.py >> /tmp/maven-proxy.log 2>&1 &
echo "Maven proxy setup complete (PID: $!)"
