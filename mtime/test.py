import os

file_dir = '/home/hadoop/tupu0/'
for root , dirs , files in os.walk(file_dir):
    for dirname in dirs:
        if dirname == "image":
            continue
        print dirname