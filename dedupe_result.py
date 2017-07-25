import os
import sys
import json

if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    output = open(output_file, "w")
    itemSet = {}
    id = 0
    with open(input_file, "r") as lines:
        for line in lines:
            entry = json.loads(line.strip())
            if "detail_url" in entry and "title" in entry:
                unique_id = hash(entry["detail_url"])
                if unique_id not in itemSet:
                    itemSet.add(unique_id)
                    entry["keyWord"] = entry["keyWord"].lower()
                    entry["itemID"] = id
                    entry["keyWords"] = entry["title"].lower().split(" ")
                    id += 1
                    output.write(json.dumps(entry))
                    output.write('\n')
    output.close()
