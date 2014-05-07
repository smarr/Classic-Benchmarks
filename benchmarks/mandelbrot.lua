-- Ported from the Ruby version to Lua by Stefan Marr, 2014
--
-- Copyright © 2004-2013 Brent Fulgham
--
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
--
--   * Redistributions of source code must retain the above copyright notice,
--     this list of conditions and the following disclaimer.
--
--   * Redistributions in binary form must reproduce the above copyright notice,
--     this list of conditions and the following disclaimer in the documentation
--     and/or other materials provided with the distribution.
--
--   * Neither the name of "The Computer Language Benchmarks Game" nor the name
--     of "The Computer Language Shootout Benchmarks" nor the names of its
--     contributors may be used to endorse or promote products derived from this
--     software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
-- DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
-- FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
-- DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
-- SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
-- CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
-- OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
-- OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--
-- The Computer Language Benchmarks Game
-- http://benchmarksgame.alioth.debian.org
--
--  contributed by Karl von Laudermann
--  modified by Jeremy Echols
--  modified by Detlef Reichl
--  modified by Joseph LaFata
--  modified by Peter Zotov
--
-- http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3

local bit    = require("bit")
local lshift = bit.lshift
local bxor   = bit.bxor
local bor    = bit.bor

local function mandelbrot(size)
  local sum      = 0
  local byte_acc = 0
  local bit_num  = 0

  for y = 0, size - 1 do
    local ci = (2.0 * y / size) - 1.0

    for x = 0, size - 1 do
      local zrzr = 0.0
      local zr   = 0.0
      local zizi = 0.0
      local zi   = 0.0

      local cr = (2.0 * x / size) - 1.5
      local escape = 1

      for z = 1, 50 do
        local tr = zrzr - zizi + cr
        local ti = 2.0 * zr * zi + ci
        zr = tr
        zi = ti

        -- preserve recalculation
        zrzr = zr * zr
        zizi = zi * zi
        if zrzr + zizi > 4.0 then
          escape = 0
          break
        end
      end

      byte_acc = bor(lshift(byte_acc, 1), escape)
      bit_num  = bit_num + 1

      -- Code is very similar for these cases, but using separate blocks
      -- ensures we skip the shifting when it's unnecessary, which is
      -- most cases.
      if bit_num == 8 then
        sum      = bxor(sum, byte_acc)
        byte_acc = 0
        bit_num  = 0
      elseif x == size - 1 then
        byte_acc = lshift(byte_acc, 8 - bit_num)
        sum      = bxor(sum, byte_acc)
        byte_acc = 0
        bit_num  = 0
      end
    end
  end

  return sum
end

local function sample()
    return mandelbrot(750) == 192
end

if not sample() then
    print("Sanity check failed! Mandelbrot gives wrong result")
    os.exit(1)
end


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

local iterations   = 100
local warmup       = 0
local problem_size = 750

if #arg >= 1 then
	iterations = tonumber(arg[1])
end

if #arg >= 2 then
	warmup = tonumber(arg[2])
end

if #arg >= 3 then
	problem_size = tonumber(arg[3])
end

print("Overall      iterations:", iterations)
print("Warmup       iterations:", warmup)
print("Problem Size iterations:", problem_size)

for i = 1, warmup do
	mandelbrot(problem_size)
end

for i = 1, iterations do
	local start = microseconds()
	mandelbrot(problem_size)
	local elapsed = microseconds() - start
	print(string.format("Mandelbrot: iterations=1 runtime: %dus", elapsed))
end
