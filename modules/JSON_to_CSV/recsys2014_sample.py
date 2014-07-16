import json
import random

def read_the_dataset(the_dataset_file):
    tweets = list()
    header = True
    with file(the_dataset_file,'r') as infile:
        for line in infile:
            if header:
                header = False
                continue # Skip the CSV header line
            line_array = line.strip().split(',')
            user_id = line_array[0]
            item_id = line_array[1]
            rating = line_array[2]
            scraping_time = line_array[3]
            tweet = ','.join(line_array[4:]) # The json format also contains commas
            json_obj = json.loads(tweet) # Convert the tweet data string to a JSON object
            # Use the json_obj to easy access the tweet data
            # e.g. the tweet id: json_obj['id']
            # e.g. the retweet count: json_obj['retweet_count']
            tweets.append((user_id, item_id, rating, scraping_time, json_obj))
    return tweets

def read_todo_from_emtpy_file(the_dataset_file):
    todos = list()
    header = True
    with file(the_dataset_file,'r') as infile:
        for line in infile:
            if header:
                header = False
                continue # Skip the CSV header line
            line_array = line.strip().split(',')
            tweet = ','.join(line_array[4:]) # The json format also contains commas
            json_obj = json.loads(tweet) # Convert the tweet data string to a JSON object
            user_id = line_array[0]
            tweet_id = json_obj['id']
            todos.append((user_id,tweet_id)) # The todo (user,tweet) pair
    return todos

def write_the_solution_file(solutions, the_solution_file):
    lines = list()
    lines.append('userid,tweetid,engagement' + '\n')
    # Prepare the writing...
    for (user,tweet,engagement) in solutions:
        line = str(user) + ',' + str(tweet) + ',' + str(engagement) + '\n'
        lines.append(line)
    # Actual writing
    with file(the_solution_file,'w') as outfile:
        outfile.writelines(lines)

# Execution flow starts here ...
if __name__ == "__main__":
    # Read the training file
    tweets = read_the_dataset('tr.txt')

    # Read the _empty file (the task)
    todos = read_todo_from_emtpy_file('tee.txt')

    # For all (user,tweet) pairs, generate their engagement
    solutions = list()
    random.seed(1)
    for (user,tweet) in todos:
        # Random guess the engagement between 0-50
        engagement = random.randint(0,50)
        solutions.append((user,tweet,engagement))

    # Sort the solutions on user id (desc), engagement (desc) and tweet id (desc)
    solutions = sorted(solutions, key=lambda data: (-int(data[0]), -int(data[2]), -int(data[1])))

    # Write the _solution file
    write_the_solution_file(solutions, 'random_solution.dat')

    print 'done.'

