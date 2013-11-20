function y = paper(x)
 y = (6*x-2).*(6*x-2).*sin(12*x-4);
 
 % Add some gaussian noise
 y=y+0.1*y*rand(1,1);
 
end