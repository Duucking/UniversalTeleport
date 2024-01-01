import pandas.io.clipboard as cb


def getClipboard():
    return cb.paste()


def setClipboard(copy_text):
    cb.copy(copy_text)
