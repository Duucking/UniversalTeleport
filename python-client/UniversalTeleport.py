import time
from threading import Thread

from service.clipboardlistener import clipboard_listener
from service.devicecopydatalistener import device_copy_data_listener

device_copy_data_listener_thread = Thread(target=device_copy_data_listener)
clipboard_listener_thread = Thread(target=clipboard_listener)
thread_list = [device_copy_data_listener_thread, clipboard_listener_thread]


def guard_service():
    # 每60s检查一次子线程，如果线程挂掉，重新启动线程
    while True:
        for i in thread_list:
            if not i.is_alive():
                print(i.name + " is dead, restarting...")
                i.start()
            else:
                print(i.name + " is alive")
        time.sleep(10)


if __name__ == '__main__':
    print('server start init!')
    device_copy_data_listener_thread.start()
    clipboard_listener_thread.start()
    # device_copy_data_listener_thread.join()
    # clipboard_listener_thread.join()
    # guard_thread = Thread(target=guard_service)
    # guard_thread.start()
    print('server init success!')


