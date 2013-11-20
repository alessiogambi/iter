%
% Taken from the OctGPR package, demo-file. This is the "sombrero" function, we negate it to have a global minimum
%
%
function z = test_function(x, y)
  z = 4 + 3 * (1-x).^2 .* exp(-(x.^2) - (y+1).^2) ...
      + 10 * (x/5 - x.^3 - y.^5) .* exp(-x.^2 - y.^2) ...
      - 1/3 * exp(-(x+1).^2 - y.^2) ...
      + 2*sin (x + y + 1e-1*x.*y);


  z = -1 .* z;
end