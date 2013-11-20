%
% This function infer a markov model starting from the state transition sequence (sts)
% provided as input. The INPUT must be column-vector !
% The ouput of the function is be a 3-column matrix tm of the form [i j Pij],
% Pij are the frequencies of transitions between i and j over the experiments. 
%
%
function tm = infer_markov_model(sts)

global logfile;
fprintf(logfile, 'infer_markov_model\n');
% fprintf(logfile, ' %f', sts);
% fprintf(logfile, '\n');

% size(x,1) row counts
if ~(size(sts,1) >= 1 & size(sts,2) == 1)
	error('[ERROR] State sequence must be a COLUMN vector !');
end

% Remove self-transitions, i.e, sequences like "si,si,si,sj" in the sts become "si,sj" in the filter variable
% We use the diff to compute the differences at step 1, i.e, x(2)-x(1),...,x(n)-(x(n-1))

filtered=sts;
filtered(find(diff(filtered) == 0 )) = [];

% No transitions no party: if the sequences of states contains only one state
if size(filtered,1) == 1
	fprintf(logfile,'[WARN] There are no state transitions \n');
	tm=[];
	return
end

% Create the matrix hosting the transitions
mt=filtered;
mt_1=mt;
mt(end)=[];
mt_1(1)=[];

transitions=[mt mt_1];

clear mt;
clear mt_1;
clear filtered;

% Prepare the output

% Take only unique transitions
% Make room for the frequencies

tm = unique (transitions, 'rows');
tm = [tm zeros(size(tm,1),1)]; 

% Infer the model from the transitions

% Iterate over index...
% note that this is not the best implementation

for row_index = 1:size(tm,1)
	i=tm(row_index,1);
	j=tm(row_index,2);

	% Count how many transitions start from state i in the original transitions

	tot = size(find(transitions(:,1)==i),1);

	% Count how many occurrences of transition i,j
	occ = size(find(transitions(:,1)==i & transitions(:,2)==j),1);

	% Compute the frequency and store it inside tm
	tm(row_index,3)=(occ/tot);
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Print the inferred model
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
fprintf(logfile,'Inferred model: \n');
fprintf(logfile,'--------------------\n');
% Note the ' after tm !
fprintf(logfile,'(%d, %d) --> %.2f\n', tm');
fprintf(logfile,'--------------------\n');