#!/bin/bash
java -jar RankLib.jar -train $1 -test $2 -ranker 8 -metric2t NDCG@10 -metric2T NDCG@10 -save model.txt
java -jar RankLib.jar -load model.txt -rank $2 -score score.txt

