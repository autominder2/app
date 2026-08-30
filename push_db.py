import subprocess
import os

adb_path = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
db_local = r"d:\Autominder\autominder_seed.db"

# Push to /data/local/tmp
subprocess.run([adb_path, "push", db_local, "/data/local/tmp/autominder_seed.db"], check=True)
subprocess.run([adb_path, "shell", "chmod", "777", "/data/local/tmp/autominder_seed.db"], check=True)

# Copy inside run-as
subprocess.run([adb_path, "shell", "run-as", "com.autominder.app", "cp", "/data/local/tmp/autominder_seed.db", "databases/autominder.db"], check=True)
subprocess.run([adb_path, "shell", "run-as", "com.autominder.app", "rm", "-f", "databases/autominder.db-wal", "databases/autominder.db-shm"])

proc = subprocess.run([adb_path, "shell", "run-as", "com.autominder.app", "ls", "-la", "databases/"], capture_output=True, text=True)
print("Listing databases/:")
print(proc.stdout)
