%
% This function accumulates training data in the global variable 'training_data'
%
% test_execution is an object that contains
% - tm : a transition matrix in the form [i j phi] that is obtained by invoking infer_markov_models
% - id : an id of test execution (not really necessary ?)
% - params : a vector that contains the test paramters (like amplitude, freq, duration, etc)
% stateSequence must be a COLUMN VECTOR !!
%
function update_training_data(stateSequence, parameters)

% Take back the reference to the global var
global training_data;
global logfile;

fprintf(logfile,'\nupdate_training_data:\n');
fprintf(logfile,'\t inputs:');
fprintf(logfile,' %d ', parameters);
fprintf(logfile,'\n');
fprintf(logfile,'\t states:');
fprintf(logfile,' %d ', stateSequence);
fprintf(logfile,'\n');

% Infer the model from the input state sequence
tm = infer_markov_model(stateSequence);

% Update training data info
[iMax, jMax]=size(training_data);
for i = 1:iMax
    for j = 1:jMax
        
        % Check if there are transitions first then update phi
        if( ~isempty( tm ) )
            if ( find( tm(:,1) == i & tm(:,2) == j ) )
                phi = tm( find( tm(:,1) == i & tm(:,2) == j ), 3);
            else
                phi = 0;
            end
        else
            fprintf('Empty TM!\n');
            phi = 0;
        end
        
        test_data.parameters=parameters;
        test_data.phi=phi;
        
        % fprintf('updating %d,%d  with %f\n', i, j, phi);
        training_data{i,j}=[training_data{i,j} test_data];
    end
end