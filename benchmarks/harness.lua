local ffi = require("ffi")

ffi.cdef[[
  typedef long time_t;
  typedef struct timeval {
    time_t tv_sec;
    time_t tv_usec;
  } timeval;

  int gettimeofday(struct timeval* t, void* tzp);
]]

local function microseconds()
  local t = ffi.new("timeval")
  ffi.C.gettimeofday(t, nil)
  return tonumber(t.tv_sec) * 1000 * 1000 + tonumber(t.tv_usec)
end

local function parse_args(arg, iterations, warmup, innerIter)
  if #arg >= 1 then
    iterations = tonumber(arg[1])
  end

  if #arg >= 2 then
    warmup = tonumber(arg[2])
  end

  if #arg >= 3 then
    innerIter = tonumber(arg[3])
  end

  print("Overall iterations:", iterations)
  print("Warmup  iterations:", warmup)
  print("Inner   iterations:", innerIter)

  return iterations, warmup, innerIter
end

return microseconds, parse_args
