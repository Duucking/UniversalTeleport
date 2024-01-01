from plyer import notification


def send_notification(title, message):
    notification.notify(
        title=title,
        message=message,
        app_icon=None,
        timeout=1
    )
