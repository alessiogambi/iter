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
function test_max_ei_ga()

clear all
close all
clc

% LOAD GPML Framework
addpath ('/home/alessio/Documents/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/main/resources/gpml');
startup

tol = 1e-4;
samples=5;

LB=[0];
UB=[1];


% Define the training data
x=linspace(0,1,50)';
n = rand(length(x),1);
[garbage index] = sort(n);
x_randomized = x(index);

xt=x_randomized(1:samples);
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
hyp.mean = 0.0;

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

ei = [1000];

x=linspace(0,1,500)'; %% ONLY FOR PLOTTING

    while max(ei) > tol

        % Train the ORIGINAL interpolator
        
        GPM.hyp=minimize(GPM.hyp_init, @gp, -200, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt);
        
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
        neg_exp_imp = @(x_test) -1 * expected_improvement(GPM_hat, x_test, y_min);
      
        % Optimize via the standard ga provided with lower and upper bounds
        % on the input (Assume user provide them!)
        x_max_ei = ga(neg_exp_imp,D,[],[],[],[],LB,UB);
      
        % For plotting to console COMPUTE the ei value, as the optimization
        % via ga does not return that !
        max_ei = expected_improvement(GPM_hat, x_max_ei, y_min);
        fprintf(1, 'MAX EXPECTED IMPROVEMENTS IS %f in location %f\n\n', max_ei, x_max_ei );
        
        % Check if we expect to improved at least the minimum value
        % if not stop the search and plot the x,y location
        % corresponding to the 'global' minimum
        
        if( max_ei < tol )
            x_min=GPM.xt( find(GPM.yt == min( GPM.yt) ) );
            fprintf(1, 'Failed to improve over the min tol.');
            fprintf(1, 'Search is over. Min is (%f, %f)\n\n', x_min, y_min);
            return
        end

        % For this 1 dim case just plot everything
       figure()
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
        
        % Collect the new SAMPLE
        GPM.xt=[GPM.xt; x_max_ei];
        GPM.yt=[GPM.yt; paper(x_max_ei) ];
        y_min = min (GPM.yt);
    end
end