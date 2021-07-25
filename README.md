# edu-java

Play vs. computer opponent a variety of Tic-Tac-Toe variations like 3-D, Ultimate T-T-T.

With algorithms like Minimax, MCTS.

## Build and run 3D Tic-Tac-Toe (uses MiniMax Algorithm)
```
$ cd src
$ ./make_jar_ult
$ java -jar ../bin/UltimateTicTacToe.jar
```
## Build and run Ultimate Tic-Tac-Toe (uses MCTS Algorithm)
```
$ cd src
$ ./make_jar_3d
$ java -jar ../bin/3DTTT.jar
```

## To Do
* Check why simple 3x3 "Tic" loses, no matter what explocation parameter "C" I try.
* Change to use snapshot of the board before rollout, instead of undoing the moves.
