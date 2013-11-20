%
% TODO Add an input to decide if minimization or maximization problem

% TODO : notify error if the interpolator cannot be created ?
%if( max_ei == 0 )
%    fprintf(logfile, 'Do another round, this is a sympthom that the interpolator was not created \n')
%    continue;
%end


% This function use a user-defined discretization over the input space (via the nBins input);
% thus is supposed to speedup the overall process, but results in termination issues 
% of the Max(E[I]) search. To tackle those, we adopt a simple/naive
% heuristic that follow the simulating annealing approach: we restart the
% search by providing a new random point 'far away' from the problematic
% bin of the search.
%
function [xt_opt, max_ei] = maximize_ei(GPM, LB, UB, nBins)

global logfile;
% fprintf(logfile, 'maximize_ei(GPM, LB, UB, nBins)\n');

% Current Optimum Value, opt -> minimization of the function!

% TODO min or max depending on the input
yt_opt = min( GPM.yt );

D=size(GPM.xt,2);

% Setup the GA Options variable and the discretization

% We do the search over the bins, and we transform the original space
% into a discretized on. NOTE: Bins are indexed starting from zero
discLB=[0];
discUB=[nBins-1];

Bound = [discLB;discUB];

% We define utility functions that map in and from the bins space
% They let us recover the 'real' value out of a bind, for example
% to compute the 'real' fitness function, and viceversa.
% By default we take the middle point of the bin.

binsToReals=@(binIDs) get_real_values(nBins, LB, UB, binIDs);
realsToBins=@(reals) discretize(nBins, LB, UB, reals);

% Build the integer GA options set: see the ga_demo.m file on the matlab
% website
%
options = gaoptimset('CreationFcn',@int_pop,...
    'MutationFcn',@int_mutation,...
    'PopInitRange', Bound,...
    'Display','off',...
    'StallGenL',40,...
    'Generations',150,...
    'PopulationSize',60);

% Predict yt_hat with the ORIGINAL interpolator to create the NOISE
% FREE version, called GPM_hat
[y_hat junk] = gp(GPM.hyp, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt, GPM.xt);

% Mock up the REINTERPOLATION to compute the noise free Max(EI)
GPM_hat.meanfunc=GPM.meanfunc;
GPM_hat.covfunc=GPM.covfunc_hat; %% NOTE THIS - must be defined in the original interpolator
GPM_hat.likfunc=GPM.likfunc;
GPM_hat.xt = GPM.xt;
GPM_hat.yt = y_hat; %% NOTE THIS - now we use the new one, not the origina, noisy one

% Hyper parameters
hyp_hat.mean = GPM.hyp.mean;
hyp_hat.cov = GPM.hyp.cov(1,1:end-1); %% NOTE THIS: We assume that the noise term is ALWAYS the last one !
hyp_hat.lik = GPM.hyp.lik;

GPM_hat.hyp = hyp_hat;

% (Re)Formulate the Max(EI) as the equivalent Min( - EI ). Use yt_opt as
% current optimal value
% ORIGINAL VERSION: neg_exp_imp = @(x_test) -1 * expected_improvement(GPM_hat, x_test, yt_opt);
% INTEGER VERSION: note that we use bins as inputs (x)!
fitnessFcn= @(x_test) -1 * expected_improvement(GPM_hat, binsToReals(x_test), yt_opt);

% Optimize EI via the standard GA provided with lower and upper bounds
% on the input
try
    
    % Optimize in the integer space
    [disc_x_max_ei,fval] = ga(fitnessFcn,D,options);
    % ORIGINAL VERSION: x_max_ei = ga(neg_exp_imp,D,[],[],[],[],LB,UB);
        
    % CONVERT BACK TO REAL VALUES 
    x_max_ei = binsToReals(disc_x_max_ei);
        
    % return everything back
    xt_opt=x_max_ei;
    max_ei = expected_improvement(GPM_hat, x_max_ei, yt_opt);
    
	fprintf(logfile, 'max(E[I]) is %.4f, inside bin [', max_ei);
	fprintf(logfile, ' %.2f', disc_x_max_ei);
	fprintf(logfile, ' ] that correspond to original input [');
    fprintf(logfile, ' %f', x_max_ei);
    fprintf(logfile, ' ]\n');
    
catch err
    fprintf(logfile, 'ERROR: Failed optimization !!!' );
    err
    global settings;
    xt_opt=settings.LB;
    max_ei = -1;
end

% UTILITY FUNCTIONS FOR THE DISCRETE GA OPTIMIZATION
%---------------------------------------------------
% TODO Use the hist function ?
function binIDs = discretize(nBins, LB, UB, real_values)
binsSize=(UB-LB)./nBins;
binIDs = floor( real_values ./binsSize );
% End of creation function
%---------------------------------------------------
function real_values = get_real_values(nBins, LB, UB, binIDs)
binsSize=(UB-LB)./nBins;
real_values= LB + binsSize .* (binIDs+0.5);
% End of creation function
%---------------------------------------------------
% Mutation function to generate childrens satisfying the range and integer
% constraints on decision variables.
function mutationChildren = int_mutation(parents,options,GenomeLength, ...
    FitnessFcn,state,thisScore,thisPopulation)
shrink = .01;
scale = 1;
scale = scale - shrink * scale * state.Generation/options.Generations;
range = options.PopInitRange;
lower = range(1,:);
upper = range(2,:);
scale = scale * (upper - lower);
mutationPop =  length(parents);
% The use of ROUND function will make sure that childrens are integers.
mutationChildren =  repmat(lower,mutationPop,1) +  ...
    round(repmat(scale,mutationPop,1) .* rand(mutationPop,GenomeLength));
% End of creation function
%---------------------------------------------------
% GENERATE THE INITIAL POPULATION
function Population = int_pop(GenomeLength,FitnessFcn,options)

totalpopulation = sum(options.PopulationSize);
range = options.PopInitRange;
lower= range(1,:);
span = range(2,:) - lower;
% The use of ROUND function will make sure that individuals are integers.
Population = repmat(lower,totalpopulation,1) +  ...
    round(repmat(span,totalpopulation,1) .* rand(totalpopulation,GenomeLength));
% End of creation function
