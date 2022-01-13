
import praw
from praw.models import MoreComments

reddit = praw.Reddit(
    client_id="PWv9-Y1uHIyysITu4KDV0g",
    client_secret="PHKu9MWfh2rwjHSK4YKmoptCKv2coQ",
    user_agent="ben my user agent",
    username="benl5442",
    password="Red123@@z",
)
"""
# assume you have a reddit instance bound to variable `reddit`
subreddit = reddit.subreddit("redditdev")

print(subreddit.display_name)
# Output: redditdev
print(subreddit.title)
# Output: reddit development
print(subreddit.description)
# Output: a subreddit for discussion of ...

for submission in subreddit.hot(limit=10):
    print(submission.title)

for comment in reddit.subreddit("Johntesting").comments():
        print(comment.body)
"""
trigger_phrase = "Pick of the Day"
subreddit = reddit.subreddit('sportsbook')
subreddit2 = reddit.subreddit('johntesting')


for submission in subreddit.new(limit=None):
    if trigger_phrase in submission.title:
        for top_level_comment in submission.comments:
            if isinstance(top_level_comment, MoreComments):
                continue
            print(top_level_comment.body)
            print('\n\n---------------------------\n')
        break

