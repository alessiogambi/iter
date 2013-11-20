function retrain_interpolators()

% Take back the global vars
global interpolators;
global training_data;
global logfile;

fprintf(logfile,'\nretrain_interpolators()\n');

% Brutally retrain all the interpolators cells, actually a cellarray function should be used here!
[I, J] = size(interpolators);
for i = 1:I
    for j = 1:J

		if ( i > j )

			% Check if there is training data. No data, no interpolator !
			if ( isempty( training_data{i,j} ) == 1 )
				fprintf(logfile, '[WARN] Empty dataset for (%d,%d) , skip interpolator !\n', i, j);
				continue;
			else
				% Take the interpolator or create a new one
				if( isempty ( interpolators{i,j} ) == 1 )
					% TODO Make a Function here !! GPM = create_default_interpolator(D)
%---------------------------------------------------------------------------------------------------------
					% Create a new interpolator
					fprintf(logfile, '[INFO] Create interpolator (%d,%d)\n', i,j);
					% Input Dimensionality is defined by the number of
					% parameters in the test
					D=size(training_data{i,j}(1).parameters,2);
                
					% Mean function as 0.5. Default value... TODO this must be
					% 1/n where n is the possible number of outgoing
					% transitions from the state. 0 is also a common
					% alternative
					meanfunc = @meanConst;
					hyp.mean = 0.5; % size=1 by definition
					% Cov function as noise+SE, and hat version without noise
					covfunc={@covSum, {@covSEard, @covNoise}};
					covfunc_hat=@covSEard;
					hyp.cov=zeros(1,(D+2)); % size=D+1 for SEard, and +1 for Noise
                
					% Likelihood function
					likfunc=@likGauss;
					hyp.lik=log(0.1); % size=1 by definition
                
					% During the retraining reuse the last values !
					GPM.hyp=hyp;
                
					GPM.meanfunc=meanfunc;
					GPM.covfunc=covfunc;
					GPM.covfunc_hat=covfunc_hat;
					GPM.likfunc=likfunc;
%---------------------------------------------------------------------------------------------------------

					interpolators{i,j}=GPM;
					clear GPM;                
				end
            
				% Current Interpolator
				interpolator=interpolators{i,j};
            
				% Extract Training data from training_data
				t_data=training_data{i,j};
            
				xt=[];
				yt=[];
				for ind = 1:size(t_data,2)
					xt = [xt; t_data(ind).parameters];
					yt = [yt; t_data(ind).phi];
				end
            
				% Update the model with new data
				% This modification is not propagated ?
				%interpolator.xt=xt;
				%interpolator.yt=yt;
				interpolators{i,j}.xt=xt;
				interpolators{i,j}.yt=yt;
            
				% If there is no variance in the output data, for example, all data
				% are equals to zero, do not train the interpolator !
				if( ~ var(yt) )
					fprintf(logfile, '[WARN] No Variation in the data for (%d,%d) , skip interpolator !\n', i, j);
					continue;
                else
%                   fprintf(logfile, 'Variation in the data for (%d,%d) ', i, j);
%                  fprintf(logfile, '%f, ', var(yt));
%                 fprintf(logfile, '\n');
%				disp(var(yt))
				end
            
				try
					fprintf(logfile, 'Retrain interpolator (%d,%d)\n', i, j);
					% Train the interpolator
					tic;
					hyp=minimize(interpolator.hyp, @gp, -200, @infExact, interpolator.meanfunc, interpolator.covfunc, interpolator.likfunc, xt, yt);
					elapsedTime = toc;
					fprintf(logfile, 'Done in %.4f seconds\n',elapsedTime);

					% Update the model with new params
					%interpolator.hyp=hyp;
					interpolators{i,j}.hyp=hyp;

					
				catch err
					fprintf(logfile,'[ERROR] %s\n',err.message);
					% following lines: stack
					for e=1:length(err.stack)
						fprintf(logfile,'%sin %s at %i\n',txt,err.stack(e).name,err.stack(e).line);
					end
				end
			end
        else
			% disp(i);
			% fprintf(logfile, 'Skip Transition %d,%d\n', i,j);
		end
    end
end
