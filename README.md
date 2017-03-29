# Eager task initialization in sbt

This is a reproduction of an error in how tasks are initialized when they run
commands. It unveils an intricate bug of sbt in which if a body of a task X refers
to a task Y, task Y will be initialized even if there are other statements in
task X that should be run before.

This can be reproduced in 0.13.13. To reproduce, run `sbt initExec`.
