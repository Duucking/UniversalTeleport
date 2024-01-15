import os
import sys

from plyer import notification

if __name__ == '__main__':
    title = sys.argv[1]
    message = sys.argv[2]
    notification.notify(
        title=title,
        message=message,
        app_icon=os.path.abspath(".") + "\\resource\\icon.ico",
        timeout=3
    )
