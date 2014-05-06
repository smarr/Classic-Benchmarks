-- Copyright 2011 Google Inc.
--
-- This program is free software; you can redistribute it and/or
-- modify it under the terms of the GNU General Public License
-- as published by the Free Software Foundation; either version 2
-- of the License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program; if not, write to the Free Software
-- Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

function class(super)
	 local C = {}

	 if super then
			C.super = super
			setmetatable(C, super.mt)
	 end

	 C.mt = { __index = C }

	 function C.new(...)
			local obj = {}
			setmetatable(obj, C.mt)
			C.constructor(obj, ...)
			return obj
	 end

	 return C
end

Benchmark = class()

function Benchmark:constructor (_, run)
		self.run = run
end

BenchmarkSuite = class()

function BenchmarkSuite:constructor(a, b, benchmarks)
		self.benchmarks = benchmarks
end

function BenchmarkSuite:run()
		for i = 1, #self.benchmarks do self.benchmarks[i].run() end
end

local type = ...
if type ~= 'locals' and type ~= 'globals' then error 'Usage: lua deltablue-10000iterations.lua locals|globals' end
assert(loadfile('deltablue-' .. type .. '.lua'))()

for i = 1, 10000 do
		DeltaBlue:run()
end
