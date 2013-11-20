%
% This function retrains the interpolators, and compute the max E[i] for all
% of them. It then take up to n top values above the minimum level of
% improvements.
%
%   IMPORTANT: By using the discretization this function may return already
%   collected samples, this means that the entire process may not
%   terminate. We must deal with it in the caller !!!!
%
function max_EIs = get_best_expected_improvements(n)

global logfile;
fprintf(logfile, 'get_best_expected_improvements: max result is %d\n', n);

retrain_interpolators();

% Take back the reference to the global var
global training_data;
global interpolators;
global settings;


max_EIs=[];
% For each element that has an interpolator compute the maxEI
[iMax, jMax]=size(training_data);
for i = 1:iMax
    for j = 1:jMax
        
        % ONLY Scale Down
        if( i > j )
            
            % No data/interpolator no party ;)
            if( ~var(interpolators{i,j}.yt) )
                % fprintf(logfile, '[WARN] No variance in the data, skip transition %d,%d\n',i,j);
                continue
            end
            
            fprintf(logfile, 'Computing Max(E[I]) for %d,%d\n', i, j);
tic;
            [xt_opt, max_ei ] = maximize_ei( interpolators{i,j} , settings.LB, settings.UB, settings.nBins);
etime=toc;
            fprintf(logfile, 'Done in %.4f seconds\n', etime);

            % Note that this may generate an error.
            if( max_ei >= settings.min_ei)
                % fprintf(logfile, 'Max(E[I])=%f within min tol %f\n', max_ei, settings.min_ei);
                max_EIs=[ max_EIs; i j max_ei xt_opt];
            else
                fprintf(logfile, '[WARN] Max(E[I])=%f is less than min tol %f\n', max_ei, settings.min_ei);
            end
            
        else
            % fprintf(logfile, 'Skip NON SCALING DOWN transition %d --> %d\n', i,j);
            continue
        end
    end
end

if( size(max_EIs,2) > 1 )
    % Order by max_ei DESC, which is the 3rd column
    max_EIs= -sortrows( -max_EIs, 3);
    % Take only up to the first n rows
    if( size(max_EIs, 1) > n)
        max_EIs=max_EIs(1:n,:)
    end
else
    fprintf(logfile, '[WARN] Cannot found any valid max(E[I])!\n');
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Print the inferred model
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
fprintf(logfile, '\nSummary of optimization:\n');
for row = [1:1:size(max_EIs, 1)]
	fprintf(logfile, '------------------------------------------------------------\nmax(E[I])=%.4f for transition (%d,%d) is in bin [', max_EIs(row,3),max_EIs(row,1),max_EIs(row,2));
	fprintf(logfile, ' %.4f', max_EIs(row, 4:end));
	fprintf(logfile, ' ]\n');
end
fprintf(logfile, '------------------------------------------------------------\n');