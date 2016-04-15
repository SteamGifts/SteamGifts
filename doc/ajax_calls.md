# Entering a Giveaway

## Request

    POST https://www.steamgifts.com/ajax.php

    do: entry_insert
    code: abcde # giveaway id
    xsrf_token: 123456789abcdef

## Response (success, JSON)

    {
      "entry_count":"933", # how many people are now entered into this giveaway
      "type":"success",
      "points":"20" # remaining points after having left the giveaway
    }

# Leaving a Giveaway

## Request

    POST https://www.steamgifts.com/ajax.php

    do: entry_delete
    code: abcde # giveaway id
    xsrf_token: 123456789abcdef

## Response (success, JSON)

    {
      "entry_count":"933", # how many people are now entered into this giveaway
      "type":"success",
      "points":"20" # remaining points after having left the giveaway
    }

# Posting a comment

## Request

    POST https://www.steamgifts.com/giveaway/giveawayId/gameName

    do: comment_new
    xsrf_token: 12456789abcdef
    parent_id: abcde # parent comment id
    description: test # what to post

## Response (success, HTTP redirect)

    Status Code: 301 Moved Permanently
    Location: /go/comment/eIE7KwI

# Removing a comment

    POST https://www.steamgifts.com/ajax.php

    xsrf_token: 123456789abcdef
    do: comment_delete # comment_undelete to undelete
    allow_replies: 1 # unknown
    comment_id: 41587307 # id of the comment to remove; NOT the perma-link

## Response (success, JSON)

    {
      "type":"success",

      # Rendered HTML for the comment
      "comment":"<div ....>"
    }
