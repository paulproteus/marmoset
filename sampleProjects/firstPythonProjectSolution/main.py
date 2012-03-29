import p1
import sys
for line in sys.stdin:
    words = line.split()
    print p1.sleep_in(words[0] == 'True',  words[1] == 'True')


