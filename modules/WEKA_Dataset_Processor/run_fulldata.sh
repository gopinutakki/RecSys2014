#!/bin/bash

java -jar GenerateRankerInput.jar beforeranking full_60_testing.arff predictions.csv
java -jar RankLib.jar -train rank_60_2_train.dat -test rank_60_2_test.dat -ranker $1 -metric2t NDCG@10 -metric2T ERR@10 -save model.txt
java -jar RankLib.jar -load model.txt -rank rank_60_2_test.dat -score myscorefile.txt
#java -jar GenerateRankerInput.jar afterranking full_60_testing.arff sorted.arff myscorefile.txt predictions.csv knn_out.csv
java -jar GenerateRankerInput.jar afterranking full_60_testing.arff full_60_testing.arff myscorefile.txt predictions.csv knn_out.csv
java -jar rscevaluator-0.14-jar-with-dependencies.jar test_solution.dat participant_solution.dat

