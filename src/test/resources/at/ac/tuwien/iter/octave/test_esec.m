%
%
% Optimize the expected improvement for the test funcion (see
% test_function.m) using genetic algorithm and the approach proposed by
% Forrester (2006)
%
% TODO : c'e' ancora qualcosa che non va perche' il modello sembra essere
% over confident, e la ricerca si ferma subito. Inoltre EI risulta 0 quando
% gli altri valori (s incluso) non lo sono !!
%
function test_esec(retries)

if nargin == 0
    fprintf(1, 'LAST RUN OF THE ALGORITHM\n\n');
    retries = 0;
end
%clear all
%close all
%clc

% LOAD GPML Framework
addpath ('/home/alessio/Documents/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/main/resources/gpml');
addpath ('/home/alessio/Documents/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/main/resources/at/ac/tuwien/iter/octave/');
startup

% Settings for Search
tol = 1e-5;
samples=5;

LB=[0];
UB=[1];

% Generate Input space
x=linspace(LB,UB/2,50)';
n = rand(length(x),1);
[garbage index] = sort(n);
x_randomized = x(index);
xt=[x_randomized(1:samples); 0.98];


D=size(xt,2);


% Discretize

% ORIGINAL LB AND UB, we need to discretize them

nBins=50;

% We do the search over the bins. Bins are indexed from zero
discLB=[0];
discUB=[nBins-1];

% We need to recover the 'real' value out of the bind to compute the
% fitness

binsToReals=@(binIDs) get_value(nBins, LB, UB, binIDs);
realsToBins=@(reals) discretize(nBins, LB, UB, reals);


Bound = [discLB;discUB];
numberOfVariables=D;

% TODO BUild the GA optimization structure !
%
%
options = gaoptimset('CreationFcn',@int_pop,...
    'MutationFcn',@int_mutation,...
    'PopInitRange', Bound,...
    'Display','off',...
    'StallGenL',40,...
    'Generations',150,...
    'PopulationSize',60);

% Define the training data

% Discretize to the bin value and take inputs back
disp(xt')

xt = binsToReals( realsToBins(xt) );

disp(xt')

% EVALUTE THE FUNCTION
yt=paper(xt);

% Original Model

D=size(xt,2);

% Training data
GPM.xt=xt;
GPM.yt=yt;

% Mean
meanfunc = @meanConst;
hyp.mean = 0.0;

GPM.meanfunc=meanfunc;

% Cov
covfunc={@covSum, {@covSEard, @covNoise}};
hyp.cov=zeros(1,(D+2)); % D+1 for SEard, and +1 for Noise

GPM.covfunc=covfunc;

% Lik
likfunc=@likGauss; hyp.lik=log(0.1);

GPM.likfunc=likfunc;

% Store the HYPER PARAMETERS
GPM.hyp_init=hyp;

clear hyp

% Noise Free Reinterpolation. It's the same of the original one but the
% covariance and yt used during the predictions

% Meanmeanfunc = @meanConst;
hyp.mean = 0.5;

% Cov - This is the trick !
covfunc=@covSEard;
hyp.cov=zeros(1,(D+1)); % D+1 for SEard

% Lik
hyp.lik=log(0.1);

GPM.covfunc_hat=covfunc;
GPM.hyp_hat=hyp;

% Initialization of the search

x_min=GPM.xt( find(GPM.yt == min( GPM.yt) ) );
y_min = min (GPM.yt);

x=linspace(0,1,500)'; %% ONLY FOR PLOTTING
iteration =0;



while 1
    % Maybe these should be randomized
    % disp( GPM.hyp_init )
    % Train the ORIGINAL interpolator
    
    GPM.hyp=minimize(GPM.hyp_init, @gp, -200, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt);
    
    
    % Try to use the previously computed values... not sure this is a good approach
    GPM.hyp_init = GPM.hyp;
    
    % Predict the yt_hat with the ORIGINAL interpolator
    
    [y_hat junk] = gp(GPM.hyp, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt, GPM.xt);
    
    % Mock up the REINTERPOLATION to compute the noise free Max(EI)
    
    GPM_hat.meanfunc=GPM.meanfunc;
    GPM_hat.covfunc=GPM.covfunc_hat; %% NOTE THIS
    GPM_hat.likfunc=GPM.likfunc;
    GPM_hat.xt = GPM.xt;
    GPM_hat.yt = y_hat; %% NOTE THIS
    
    hyp_hat.mean = GPM.hyp.mean;
    hyp_hat.cov = GPM.hyp.cov(1,1:end-1); %% NOTE THIS: We assume that the noise term is ALWAYS the last one !
    hyp_hat.lik = GPM.hyp.lik;
    
    GPM_hat.hyp = hyp_hat;
    
    % Formulate the Max(EI) as the equivalent Min( - EI ). Force the
    % optimal value up to now AND the GPM_hat model to optimize EI
    % % ORIGINAL: neg_exp_imp = @(x_test) -1 * expected_improvement(GPM_hat, x_test, y_min);
    fitnessFcn= @(x_test) -1 * expected_improvement(GPM_hat, binsToReals(x_test), y_min);
    
    % Optimize via the standard ga provided with lower and upper bounds
    % on the input (Assume user provide them!)
    
    [disc_x_max_ei,fval] = ga(fitnessFcn,numberOfVariables,options);
    % x_max_ei = ga(neg_exp_imp,D,[],[],[],[],LB,UB);
    fprintf(1, 'MAX EXPECTED IMPROVEMENTS IS in BIN %f\n', disc_x_max_ei );
    
    x_max_ei = binsToReals(disc_x_max_ei);
    
    % For plotting to console COMPUTE the ei value, as the optimization
    % via ga does not return that !
    max_ei = expected_improvement(GPM_hat, x_max_ei, y_min);
    fprintf(1, 'MAX EXPECTED IMPROVEMENTS IS %f in location %f\n\n', max_ei, x_max_ei );
    
    % For this 1 dim case just plot everything
    figure('visible','on')
    subplot(2,1,1)
    hold on
    
    % ORIGINAL FUNCTION
    plot(x, paper(x), 'Color','black');
    % SAMPLE DATA
    plot(GPM.xt, GPM.yt, 'ks');
    
    % Predictions by the ORIGINAL model
    
    GPM.hyp
    GPM_hat.hyp
    
    [y_test s2_test] = gp(GPM.hyp, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt, x);
    plot(x, y_test, 'Color','red');
    plot(x,(y_test+s2_test), 'Color','green');
    plot(x,(y_test-s2_test), 'Color','green');
    
    % Max(EI)
    plot(x_max_ei, paper(x_max_ei), 'rs');
    
    subplot(2,1,2)
    hold on
    % Predictions by the NOISE FREE model
    [y_test s2_test] = gp(GPM_hat.hyp, @infExact, GPM_hat.meanfunc, GPM_hat.covfunc, GPM_hat.likfunc, GPM_hat.xt, GPM_hat.yt, x);
    plot(x, y_test, 'Color','red');
    plot(x,(y_test+s2_test), 'Color','green');
    plot(x,(y_test-s2_test), 'Color','green');
    
    
    if( max_ei == 0 )
        fprintf(1, 'WARNING max_ei is zero !\n')
    end
    
    if( max_ei < tol )
        
        % Collect the new SAMPLE
        GPM.xt=[GPM.xt; x_max_ei];
        % THIS MAY BE NOISY
        GPM.yt=[GPM.yt; paper(x_max_ei) ];
                fprintf(1, 'Failed to improve over the min tol.');

       break;
    end
    
    % NOT SURE THIS IS RIGHT:
    % IF WE ALREADY COLLECTED THE SAMPLE WE STOP.THIS MIGHT BE CAUSED BY
    % OUR DISCRETIZATION
    
    if ismember(x_max_ei, GPM.xt,'rows');
        fprintf(1, 'We already collected this sample.');
        retries = retries -1;
        if retries < 0
            fprintf(1, ' No more restart allowed. Exit\n');
                   break;
        end
        % This is not elegant... but at least should give us a chance ?
        fprintf(1, 'Add a random sample (far away from the last X of at least 2 bins) and continue !\n');

        % TODO THE 2 is not implemented: we need a ran int that is between
        % 2 and nBins -2 so when we take the modulus we are ouside the +/-
        % 2 band aournd the disc_x_max_ei. 
        nr = rand(nBins-1,1);
        
        
        [garbage inr] = sort(nr);
        ran = inr(1,1);
        
        disc_x_max_ei = mod( disc_x_max_ei + ran, nBins);
        
        clear nr
        
        x_max_ei = binsToReals(disc_x_max_ei);
        fprintf(1, 'Random seed %f \n', disc_x_max_ei);
        
    end
    
    % Collect the new SAMPLE
    GPM.xt=[GPM.xt; x_max_ei];
    % THIS MAY BE NOISY
    GPM.yt=[GPM.yt; paper(x_max_ei) ];
    
    y_min = min (GPM.yt);
    
    %filename=sprintf('./iteration%d.eps', iteration);
    %Save to file
    % print('-depsc2', filename );
    
    iteration = iteration +1;
end
  y_min = min (GPM.yt);
  x_min=GPM.xt( find(GPM.yt == min( GPM.yt) ) );
 fprintf(1, 'Search is over. Min is (%f, %f)\n\n', x_min, y_min);


%FIND the BINs where the reals belong to
% TODO use hist function ?
function binIDs = discretize(nBins, LB, UB, real_values)
binsSize=(UB-LB)./nBins;
binIDs = floor( (real_values -LB) ./binsSize );


function real_values = get_value(nBins, LB, UB, binIDs)
binsSize=(UB-LB)./nBins;
real_values= LB + binsSize .* (binIDs+0.5);
% fprintf(1, 'Real value for %f is %f\n', binIDs, real_values);

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


