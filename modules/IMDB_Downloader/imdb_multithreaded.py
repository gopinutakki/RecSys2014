#!/usr/bin/env python

from imdb import IMDb
from imdbpie import Imdb
from multiprocessing.pool import ThreadPool
from multiprocessing import Lock
import cPickle as pickle
import sys


imdb_statement = IMDb()
imdb_more = Imdb()
movie_ids = set()
movie_information = dict()
pickle_lock = Lock()


class IMDBMovies(object):

    def __init__(self, mid):
        self.movie_info = self.store_movie_information(mid)

    def store_movie_information(self, movie):
        global movie_information

        m = dict()
        m['mid'] = movie
        m['recommendations'] = imdb_statement.get_movie_recommendations(movie)
        m['main'] = imdb_statement.get_movie_main(movie)
        m['keywords'] = imdb_statement.get_movie_keywords(movie)
        m['full_credits'] = imdb_statement.get_movie_full_credits(movie)
        m['release_dates'] = imdb_statement.get_movie_release_dates(movie)

        imdb_movie_for_update = imdb_statement.get_movie(movie)
        imdb_statement.update(imdb_movie_for_update, info=('vote details',))
        m['update'] = imdb_movie_for_update

        m['more'] = imdb_more.find_movie_by_id('tt'+movie)

        movie_information[movie] = m

        try:
            pickle_lock.acquire()
            if len(movie_information) % 1000 == 0:
                pickle.dump(movie_information, open('imdb.p', 'wb'))
                print '--> saved'

            print movie, len(movie_information)
        except:
            print "Was unable to acquire the lock"
        finally:
            pickle_lock.release()

        return m

    def get_movie_info(self):
        return self.movie_info


def get_movie_id(fname):
    f = open(fname,'r')
    for line in f.readlines():
        #id = line.split(',')[1] # use this line for the raw dataset.
        mid = line.strip()  # use this for the file with the movie_ids alone.
        movie_ids.add(mid)


def get_movie_information():
    global movie_information
    pool = ThreadPool(processes=20)
    pool.map(IMDBMovies, movie_ids)
    #m_infos = pool.map(IMDB_Movies, movie_ids)
    #m = [m_info.get_movie_infor() for m_info in m_infos]
    pickle.dump(movie_information, open('imdb.p', 'wb'))


def read_from_pickle():
    m = pickle.load(open('imdb.p', 'rb'))
    print(len(m))
    print m['1521848']['update']


if __name__ == '__main__':
    for arg in sys.argv[1:]:
        #get_movie_id(arg)
        #get_movie_information()
        read_from_pickle()