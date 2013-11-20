function test_max_ei()

clear all
close all
clc

addpath ('/home/alessio/Documents/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/main/resources/gpml');
startup

%% THIS IS PROBLEM SPECIFIC
tol=10^-4;
min_samples=5;

% Start with a rough grid
x=linspace(0,1,25)';

n = rand(length(x),1);
[garbage index] = sort(n);
x_randomized = x(index);

xt=x_randomized(1:min_samples);
yt=paper(xt);

% Refine x
x=linspace(0,1,300)';

% Initialize y_min, x_max_ei
y_min=min(yt);
x_max_ei=xt( find( yt==y_min ) );

% Setup the Interpolator
meanfunc = @meanConst; hyp.mean = 0.0;
covfunc=@covSEard; hyp.cov=[0; 0]; % D+1
likfunc=@likGauss; hyp.lik=log(0.1);

GPM.hyp_init=hyp;
GPM.meanfunc=meanfunc;
GPM.covfunc=covfunc;
GPM.likfunc=likfunc;
GPM.xt=xt;
GPM.yt=yt;

ei=[100];

while max(ei) > tol

  % Train the model
  GPM.hyp=minimize(GPM.hyp_init, @gp, -200, @infExact, GPM.meanfunc, GPM.covfunc, GPM.likfunc, GPM.xt, GPM.yt);

  % Update y_min
  y_min=min(GPM.yt);

  % Compute EI w.r.t. y_min over the whole x axis
  ei = expected_improvement(GPM, x, y_min );

  fprintf(stdout, "MAX EXPECTED IMPROVEMENTS IS %f \n\n", max(ei) );
  fflush(stdout);

  if( max(ei) == 0 )
    fprintf(stdout, "Search is over. Min is (%f, %f)\n\n", x_max_ei, y_min);
    fflush(stdout);
    return;
  endif

  % Take the corresponding x_max_ei_x
  x_max_ei=x( find( ei==max(ei) ) );

  subplot(2,1,1)
  hold on
  data=[GPM.xt GPM.yt];
  plot( x, paper(x), "0--");
  plot(data(:,1), data(:,2), "0*")
  plot( [0; 1], [y_min; y_min], "4-");
  plot( x_max_ei, paper(x_max_ei), "3o");

  subplot(2,1,2)
  plot( x_max_ei, max(ei), "3o");
  hold off

  % Update data structures
  GPM.xt=[GPM.xt; x_max_ei];
  GPM.yt=[GPM.yt; paper(x_max_ei )];


  % Wait user input
  % input ("Press a key to continue! ");
endwhile
endfunction
