import os
import sys


if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    output = open(output_file, "w")
    itemSet = {}
    with open(input_file, "r") as lines:
        for line in lines:
            line = line.strip()
            if line != " " :
                wordList = line.split(" ")
                wordList = filter(None, wordList)
                l = len(wordList)
                if l > 2 :
                    for i in range(2, l + 1):
                        for j in range(l - i + 1):
                            word = ''.join(wordList[j:j+i])
                            if word not in itemSet:
                                itemSet.add(word)
                                output.write(word)
                                output.write('\n')                                                               
                elif l > 0 and l <= 2:
                    word = ''.join(wordList)
                    if word not in itemSet:
                        itemSet.add(word)
                        output.write(word)
                        output.write('\n')
    output.close()
