import subprocess

from util import configutil


def send_notification(title, message):
    is_send_notify = configutil.read_config("Option", "isNotification") == 'True'
    if is_send_notify:
        subprocess.run('pythonw ./util/notificationutil.py ' + '"' + title + '" "' + message + '"')
