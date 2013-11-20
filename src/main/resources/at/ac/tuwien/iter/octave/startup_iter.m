%
% Define the global variables for the workspace and inizialize them with
% the size specified by problem_size. We need that info because model
% inference will found only observed transitions, and we cannot say
% anything about unobserved ones.
%
function startup_iter(problem_size, tol, min_ei, LB, UB, nBins, log_file)

% TODO Define a strucutre for the GA search: look the ga_demo.m !!

global interpolators;
interpolators{problem_size,problem_size}=[];

global training_data;
training_data{problem_size,problem_size}=[];

global settings;
settings.tol=10e-4;
settings.LB=LB;
settings.UB=UB;
settings.min_ei=min_ei;

if nargin > 5
    settings.nBins=nBins;
else
    settings.nBins = 100;
end
    
global logfile;
if nargin > 6
	[logfile err ]=fopen(log_file, 'w');
	% TODO Check the error and report to user
else
	% stdout by default
	logfile=1; 
end



%
% Summary
%
fprintf(logfile,'SUMMAY ITER STARTUP:\n');
fprintf(logfile,'\t Problem size: (%d)\n', problem_size);
fprintf(logfile,'\t Tol: (%d)\n', tol);

fprintf(logfile,'\t Min E[I]: (%d)\n', min_ei);

fprintf(logfile,'\t LB:');
fprintf(logfile,' %d',LB);
fprintf(logfile,'\n');

fprintf(logfile,'\t UB:');
fprintf(logfile,' %d',UB);
fprintf(logfile,'\n');