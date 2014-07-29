__author__ = 'gpc94156'


from imdb import IMDb
from imdbpie import Imdb
from multiprocessing.pool import ThreadPool
import multiprocessing
import cPickle as pickle
import sys
import json


def read_movie_from_pickles(path):
    print 'Reading the pickles to get all the saved movie IDs'
    c = 0
    while True:
        try:
            c += 1
            path += str(c) + '-imdb.p'
            print 'Path: ', path
            movies = pickle.load(open(path, 'rb'))
            for k in movies.keys():
                r = movies[k]['recommendations']


            print 'Done for pickle: ', c
            break
        except:
            raise

if __name__ == '__main__':
    for arg in sys.argv[1:]:
        read_movie_from_pickles(arg)

