%
%
% Optimize the expected improvement for the test funcion (see test_function.m) using simulated annealin by optim package
%
%
function test_max_ei_samin()

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


%%%% Prepare optimization

k = size(GPM.xt)(1,2); # dimensionality is defined with the number of cols

# SA controls

% Upper bounds for the inputs. Assumed to be given at setup. Must be column.
ub = [1; 1];

% Lower bounds for the inputs. Assumed to be given at setup. Must be column.
lb = [0; 0];

% # of iterations between temperature reductions
nt = 20;

% # of iterations between bounds adjustments
ns = 5;

% (0 < rt <1): temperature reduction factor
% rt = 0.5; # careful - this is too low for many problems
rt = 0.8

% maxevals - integer: limit on function evaluations
maxevals = 1e10;

% number of values final result is compared to
neps = 5;

% (> 0): the required tolerance level for function value comparisons
functol = 1e-10;

% (> 0): the required tolerance level for parameters
paramtol = 1e-3;

verbosity = 2; # only final results. Inc

% which of function args is minimization over? Cannot have both at the same time ?!
minarg = 2;

control = { lb, ub, nt, ns, rt, maxevals, neps, functol, paramtol, verbosity, 1};


% Redefine expected_improvement to use only x_test as parameters and to be negated, so the max(ei) becomes min(-ei)
y_min=min(GPM.yt);

neg_exp_imp = @(GPM, x_test) -1 * expected_improvement(GPM, x_test, y_min);

# do sa
t=cputime();

# This works
# 
#initial_x = rand(1,1);
#initial_y = rand(1,1);
#[x, obj_value, convergence] = samin("test_function", {initial_x initial_y}, control);

x_test_initial = rand(k,1);

[x, obj_value, convergence] = samin("neg_exp_imp", {GPM x_test_initial y_min}, control);

t = cputime() - t;
printf("Elapsed time = %f\n\n\n",t);

