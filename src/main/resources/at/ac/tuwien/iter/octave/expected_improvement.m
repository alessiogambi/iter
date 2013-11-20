%
% Interpolator, current optimum (x_opt) and test location (x_test) returns the expected improvement
% of x_test w.r.t the current optimum y_opt (that was also SAMPLED, there its s_opt==0). 
%
% This is the function to optimize
%
function ei = expected_improvement(interpolator, x_test, y_opt)

global logfile;
    
% minimization problem -> diff = [y_min - y_test]
% maximization problem -> diff = [y_test - y_max]

% s_test == 0 means that there is no way to improve for a location that was already sampled and was not yet the maximum
% As we work with noisy data we canno assume s_test == 0 for sampled points, so we need to create another GPML using as input our inputs
% but as OUTPUTS the prediction of our interpolators. in this way the noise in the second Kriging can be assumed to be zero at sample locations

   % Predict y_test
   [y_test s2_test] = gp(interpolator.hyp, @infExact, interpolator.meanfunc, interpolator.covfunc, interpolator.likfunc, interpolator.xt, interpolator.yt, x_test);
   delta_y=(y_opt - y_test);

   s_test=sqrt(s2_test);

   
   %fprintf(logfile, 'expected_improvement xtest= ');
   %fprintf(logfile, '%f ', x_test);
   %fprintf(logfile, 'and y_opt %f ', y_opt);
   %fprintf(logfile, 'y_test=%f, and s_test=%f ', y_test, s_test);
   
   
   if s_test == 0
	% Here the either the x_test is the opt or there is no chance of improving
	ei = 0;
   else
	% There is some space for improving
 	ei = delta_y .* normcdf( delta_y ./ s_test ) + s_test .* normpdf( delta_y ./ s_test );
   end

  

% Break Point
%input ('Press a key to continue! ');
%fprintf(logfile, ' ==> ei = %f\n', ei);
