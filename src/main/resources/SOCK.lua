
-- SOCKETS
package.path = package.path.. ';.\\Scripts\\?.lua;.\\LuaSocket\\?.lua;'

-- MODULE
SOCK = {
	-- CONST
	DEGRAD = 180 / math.pi,
	PORT = 20000,
	FRAME_DELAY = 0.05, -- seconds
	CODES = {
		ALT = "ALT",
		POSITION_LL = "PLL",
		HBP = "HBP",
		FUEL = "FUEL",
		NAME = "NAME",
		RPM = "RPM",
		ENG_TEMP = "ET",
		WAYPOINT = "WP",
		LAND = "LAND",
	},
	-- VARS
	loopVarsChunk = 0,
	lastT = 0,
	socket = nil,
	clients = {},
	-- METHODS
	start = function (self)
		local socket = require("socket")
		self.socket, err = socket.bind("*", self.PORT)
		-- non-blocking server
		self.socket:settimeout(0)
		self.socket:setoption("linger", {on = true, timeout = 5})
	end,
	-- check new connections and apply/answer commands
	beforeFrame = function (self)
		-- This is done every frame always.
		-- connection accepting muse be fast, and readAllData reacts
		-- only if there is a command from the client anyway.
		self:checkNewConnections()
		self:readAllData()
	end,
	-- notify data broadcasting
	afterFrame = function (self)
		-- This is broadcasted everytime, so put a limit in the frames.
		local newT = LoGetModelTime()
		if newT - self.lastT > self.FRAME_DELAY then
			self.lastT = newT
			-- also send data in chunks each time, cycle them
			if self.loopVarsChunk == 0 then
				-- Chunk 1: POS page
				local radarALT = LoGetAltitudeAboveGroundLevel()
				local baroALT = LoGetAltitudeAboveSeaLevel()
				local selfData = LoGetSelfData()
				if radarALT ~= nil and baroALT ~= nil then
					self:broadcast(self.CODES.ALT..":"..baroALT..":"..radarALT)
				end
				if selfData then
					self:broadcast(self.CODES.POSITION_LL..":"..selfData.LatLongAlt.Long..":"..selfData.LatLongAlt.Lat)
					self:broadcast(self.CODES.HBP..":"..(selfData.Heading * self.DEGRAD)..":"..(selfData.Bank * self.DEGRAD)..":"..(selfData.Pitch * self.DEGRAD))
				end
			elseif self.loopVarsChunk == 1 then
				-- Chunk 2: ENG page
				local engData = LoGetEngineInfo()
				local wpData = LoGetRoute()
				local landing, name = self:checklanding()
				if engData then
					local fuelLeftKG = engData.fuel_internal + engData.fuel_external
					local fuelConsumptionKGsec = engData.FuelConsumption.left + engData.FuelConsumption.right
					self:broadcast(self.CODES.FUEL..":"..fuelLeftKG..":"..fuelConsumptionKGsec)
					self:broadcast(self.CODES.RPM..":"..engData.RPM.left..":"..engData.RPM.right)
					self:broadcast(self.CODES.ENG_TEMP..":"..engData.Temperature.left..":"..engData.Temperature.right)
				end
				if wpData then
					local wpNum = wpData.goto_point.this_point_num
					local coords = LoLoCoordinatesToGeoCoordinates(wpData.goto_point.world_point.x, wpData.goto_point.world_point.z)
					if landing then
						self:broadcast(self.CODES.LAND..":"..name..":"..coords.longitude..":"..coords.latitude)
					else
						self:broadcast(self.CODES.WAYPOINT..":"..wpNum..":"..coords.longitude..":"..coords.latitude..":"..wpData.goto_point.world_point.y)
					end
				end
			end
			-- increment and reset to cycle when out of maximum
			self.loopVarsChunk = self.loopVarsChunk + 1
			if self.loopVarsChunk > 1 then
				self.loopVarsChunk = 0
			end
		end
	end,
	-- close all sockets
	stop = function (self)
		for k, c in ipairs(self.clients) do
			c:send("EXIT:0\n")
			c:close()
			table.remove(self.clients, k)
		end
		self.socket:close()
	end,
	-- finds new connections and adds them to the table (clients)
	checkNewConnections = function(self)
		if self.socket ~= nil then
			connection, err = self.socket:accept()
			if connection ~= nil then
				--new connection - non-blocking
				connection:settimeout(0)
				connection:setoption("linger", {on = true, timeout = 5})
				table.insert(self.clients, connection)
			end
		end
	end,
	-- reads all the data left from the clients
	readAllData = function(self)
		if self.clients ~= nil then
			for k, c in ipairs(self.clients) do
				local s, status = c:receive(2^10)
				if s ~= nil then
					r = self:getreply(s)
					if r ~= nil then
						c:send(r.."\n")
					end
				end
				if err == "closed" then
					table.remove(self.clients, k)
				end
			end
		end
	end,
	-- broadcast a message to all clients
	broadcast = function(self, message)
		for k, c in ipairs(self.clients) do
			c:send(message.."\n")
		end
	end,
	-- reply to a client message (return nil for no response)
	getreply = function(self, message)
		-- TODO
		return nil
	end,
	checklanding = function(self)
		local navInfo = LoGetNavigationInfo()
		local navRoute = LoGetRoute()
		local x = math.floor(navRoute.goto_point.world_point.x)
		if navInfo.SystemMode.submode == "LANDING" or navInfo.SystemMode.submode == "ARRIVAL" then
			if (x == -18893) or (x == 8070) or (x == 1329) or (x == -12152) then
				return true, "Anapa"
			elseif (x == -20469) or (x == 7301) or (x == 359) or (x == -13527) then
				return true, "Krymsk"
			elseif (x == -54298) or (x == -47609) then
				return true, "Novorossiysk"
			elseif (x == -64187) or (x == -57293) then
				return true, "Gelendzhik"
			elseif (x == -4567) or (x == 19982) or (x == 13845) or (x == 1570) then
				return true, "Krasnodar-P"
			elseif (x == 10744) or (x == 12626) or (x == 12155) or (x == 11214) then
				return true, "Krasnodar-C"
			elseif (x == -212461) or (x == -180974) or (x == -188846) or (x == -204590) then
				return true, "Gudauta"
			elseif (x == -172929) or (x == -168705) then
				return true, "Sochi"
			elseif (x == -212547) or (x == -228602) or (x == -216561) or (x == -224588) then
				return true, "Sukhumi"
			elseif (x == -280306) or (x == -283255) or (x == -281043) or (x == -282518) then
				return true, "Senaki"
			elseif (x == -289850) or (x == -279928) or (x == -282409) or (x == -287370) then
				return true, "Kutaisi"
			elseif (x == -43567) or (x == -58952) or (x == -47414) or (x == -55106) then
				return true, "Mineralnye-V"
			elseif (x == -40432) or (x == -12454) or (x == -19449) or (x == -33437) then
				return true, "Maykop"
			elseif (x == -114735) or (x == -119831) then
				return true, "Nalchik"
			elseif (x == -85786) or (x == -81251) or (x == -82385) or (x == -84652) then
				return true, "Mozdok"
			elseif (x == -147492) or (x == -149690) or (x == -148042) or (x == -149141) then
				return true, "Beslan"
			elseif (x == -345359) or (x == -350596) then
				return true, "Batumi"
			elseif (x == -324114) or (x == -311802) or (x == -314880) or (x == -321036) then
				return true, "Kobuleti"
			elseif (x == -306227) or (x == -331898) or (x == -312645) or (x == -323887) then
				return true, "Vaziani"
			elseif (x == -304472) or (x == -326635) or (x == -310013) or (x == -321094) then
				return true, "Lochini"
			elseif (x == -305724) or (x == -329942) or (x == -311778) or (x == -325480) then
				return true, "Soganlug"
			end
			return true, "<LAND>"
		end
		return false, nil
	end,
}

-- HOOKS
do
	local __LuaExportStart = LuaExportStart
	local __LuaExportBeforeNextFrame = LuaExportBeforeNextFrame
	local __LuaExportAfterNextFrame = LuaExportAfterNextFrame
	local __LuaExportStop = LuaExportStop
	LuaExportStart = function ()
		if __LuaExportStart then
			__LuaExportStart()
		end
		SOCK:start()
	end
	LuaExportBeforeNextFrame = function ()
		if __LuaExportBeforeNextFrame then
			__LuaExportBeforeNextFrame()
		end
		SOCK:beforeFrame()
	end
	LuaExportAfterNextFrame = function ()
		if __LuaExportAfterNextFrame then
			__LuaExportAfterNextFrame()
		end
		SOCK:afterFrame()
	end
	LuaExportStop = function ()
		if __LuaExportStop then
			__LuaExportStop()
		end
		SOCK:stop()
	end
end

--[[

function table.val_to_str ( v )
  if "string" == type( v ) then
    v = string.gsub( v, "\n", "\\n" )
    if string.match( string.gsub(v,"[^'\"]",""), '^"+$' ) then
      return "'" .. v .. "'"
    end
    return '"' .. string.gsub(v,'"', '\\"' ) .. '"'
  else
    return "table" == type( v ) and table.tostring( v ) or
      tostring( v )
  end
end

function table.key_to_str ( k )
  if "string" == type( k ) and string.match( k, "^[_%a][_%a%d]*$" ) then
    return k
  else
    return "[" .. table.val_to_str( k ) .. "]"
  end
end

function table.tostring( tbl )
  if "table" ~= type(tbl) then
    return
  end
  local result, done = {}, {}
  for k, v in ipairs( tbl ) do
    table.insert( result, table.val_to_str( v ) )
    done[ k ] = true
  end
  for k, v in pairs( tbl ) do
    if not done[ k ] then
      table.insert( result,
        table.key_to_str( k ) .. "=" .. table.val_to_str( v ) )
    end
  end
  return "{" .. table.concat( result, "," ) .. "}"
end

function table.print(t)
	local f = io.open(lfs.writedir()..'Scripts/dump.txt', "w")
	if f then
		f:write(table.tostring(t))
		f:close()
	end
end

]]--