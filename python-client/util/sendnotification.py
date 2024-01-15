import os

from util import configutil


def send_notification(title, message):
    is_send_notify = configutil.read_config("Option", "isNotification") == 'True'
    if is_send_notify:
        os.system('python ./util/notificationutil.py ' + '"' + title + '" "' + message + '"')
