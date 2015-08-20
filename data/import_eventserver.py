"""
Import sample data for similar product engine
"""

import predictionio
import argparse
import random

SEED = 7

def import_events(client):
  random.seed(SEED)
  count = 0
  print client.get_status()
  print "Importing data..."

  # generate 10 posts, with ids 1 through 10 and give them a random likeCount
  post_ids = ["%s" % i for i in range(1, 11)]
  for post_id in post_ids:
    likeCount = random.randint(1,30)
    print "Set post", post_id, "with like count of", likeCount
    client.create_event(
      event="$set",
      entity_type="post",
      entity_id=post_id,
      properties={
        "likeCount": likeCount,
      }
    )
    count += 1

  print "%s events were imported." % count

if __name__ == '__main__':
  parser = argparse.ArgumentParser(
    description="Import sample data for similar product engine"
  )
  parser.add_argument('--access_key', default='invalid_access_key')
  parser.add_argument('--url', default="http://localhost:7070")

  args = parser.parse_args()
  print args

  client = predictionio.EventClient(
    access_key=args.access_key,
    url=args.url,
    threads=5,
    qsize=500
  )
  
  import_events(client)
