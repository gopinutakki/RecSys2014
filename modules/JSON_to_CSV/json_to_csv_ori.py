#!/usr/bin/env python

import json, sys, unicodecsv
__author__ = 'gopi'


# get the filename from the arguments
filename = '2.txt'

# open the file and skip first line to get correct json entry point, due to special twitter format of the js file
inputFile = open(filename)
inputFile.readline()
data = json.load(inputFile)
input.close()

# create a output file
out = open(filename[:-2]+"csv","wb")

output = unicodecsv.writer(out, encoding='utf-8')
output.writerow(["id"]+["timestamp"]+["tweet"]+["isRetweet"]+["author"])
for row in data:

    try:
        tweet = row['retweeted_status']['text']
        retweet = True
    except KeyError:
        tweet = row['text']
        retweet = False

    try:
        author = row['retweeted_status']['user']['screen_name']
    except KeyError:
        author = "me"

    tweet = tweet.replace("\n"," ")
    tweet = tweet.replace("&gt;",">")
    tweet = tweet.replace("&lt;","<")
    tweet = tweet.replace("&amp;", "&")
    tweet = tweet.replace("\"","'")

    output.writerow([row["id"],row["created_at"],tweet,retweet,author])

out.close()
