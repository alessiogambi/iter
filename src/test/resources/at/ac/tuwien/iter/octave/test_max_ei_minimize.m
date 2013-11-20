%
%
% Optimize the expected improvement for the test funcion (see test_function.m) using the minimize function
%
%
function test_max_ei_minimize()

clear all
close all
clc

addpath ('/home/alessio/Documents/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/main/resources/gpml');
startup

% Define the interpolator
meanfunc = @meanConst; hyp.mean = 0.0;
covfunc=@covSEard; hyp.cov=[0; 0; 0]; % D+1, D=2
likfunc=@likGauss; hyp.lik=log(0.1);

GPM.hyp_init=hyp;
GPM.meanfunc=meanfunc;
GPM.covfunc=covfunc;
GPM.likfunc=likfunc;

% Training data

x=linspace(0,1,50)';

% Define 1 dimension
n = rand(length(x),1);
[garbage index] = sort(n);
x_randomized = x(index);

xt1=[x_randomized(1:20)];

% Define 2 dimension
n = rand(length(x),1);
[garbage index] = sort(n);
x_randomized = x(index);

xt2=[x_randomized(1:20)];

xt=[xt1 xt2];
yt=test_function(xt1, xt2);

GPM.xt=xt;
GPM.yt=yt;

% Train the interpolator
GPM.hyp=minimize(GPM.hyp_init, @gp, -200, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt);

%%%% Prepare optimization: Express them as minimization

y_min = min (GPM.yt);

neg_exp_imp = @(x_test) -1 * expected_improvement(GPM, x_test, y_min);

k=2;
x_initial=rand(k,1)
[x v nev] = minimize("neg_exp_imp", {x_initial});


endfunction
