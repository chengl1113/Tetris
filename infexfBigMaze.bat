@echo off

REM Set the command to be executed
set command=java -cp "./lib/*;." edu.cwru.sepia.Main2 data/labs/pitfall/medium/16x16.xml

REM Number of times to run the command
set num_runs=100

REM Initialize a counter
set /a win_count=0

REM Loop to execute the command
for /L %%i in (1, 1, %num_runs%) do (
    echo Running command %%i
    @REM %command%
    %command% | find "BayesianAgent won!" > nul && set /a win_count+=1
)

REM Display the total number of wins
echo Total number of wins: %win_count%
