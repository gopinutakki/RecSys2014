#!/usr/bin/env python

from imdb import IMDb
from imdbpie import Imdb
import pickle
import sys

imdb_statement = IMDb()
imdb_more = Imdb()
movie_ids = set()
movie_information = dict()

def get_movie_id(fname):
    f = open(fname,'r')
    for line in f.readlines():
        #id = line.split(',')[1] # use this line for the raw dataset.
        id = line.strip() # use this for the file with the movie_ids alone.
        movie_ids.add(id)


def get_movie_information():

    i = 1
    for movie in movie_ids:
        movie_information[movie] = {}
        movie_information[movie]['recommendations'] = imdb_statement.get_movie_recommendations(movie)
        movie_information[movie]['main'] = imdb_statement.get_movie_main(movie)
        movie_information[movie]['keywords'] = imdb_statement.get_movie_keywords(movie)
        movie_information[movie]['full_credits'] = imdb_statement.get_movie_full_credits(movie)
        movie_information[movie]['release_dates'] = imdb_statement.get_movie_release_dates(movie)
        movie_information[movie]['more'] = imdb_more.find_movie_by_id('tt'+movie)
        if i % 100 == 0:
            pickle.dump(movie, open('imdb.p', 'wb'))

if __name__ == '__main__':
    for arg in sys.argv[1:]:
        get_movie_id(arg)
        get_movie_information()
