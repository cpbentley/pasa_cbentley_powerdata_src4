A Merkle Tree exchange would look like this:

    Start with the root of the tree (a list of one hash value).
    The origin sends the list of hashes at the current level.
    The destination diffs the list of hashes against its own and then requests subtrees that are different. If there are no differences, the request can terminate.
    Repeat steps 2 and 3 until leaf nodes are reached.
    The origin sends the values of the keys in the resulting set.
